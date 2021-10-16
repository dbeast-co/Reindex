package com.dbeast.reindex.project_execution_plan_runner;

import com.dbeast.reindex.constants.EProjectStatus;
import com.dbeast.reindex.data_warehouse.DataWarehouse;
import com.dbeast.reindex.elasticsearch.ElasticsearchController;
import com.dbeast.reindex.elasticsearch.ElasticsearchDbProvider;
import com.dbeast.reindex.exceptions.ClusterConnectionException;
import com.dbeast.reindex.reindex_execution_plan_builder.dao.IClusterTaskDAO;
import com.dbeast.reindex.reindex_execution_plan_builder.reindex_plan.ReindexPlanPOJO;
import com.dbeast.reindex.reindex_execution_plan_monitoring.ProjectStatusPOJO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.client.RestHighLevelClient;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

class ReindexProjectExecutor implements Runnable {
    private static final Logger logger = LogManager.getLogger();

    private final ReindexPlanPOJO plan;
    private final ThreadPoolExecutor indicesThreadPool;
    private final List<ReindexJobExecutor> reindexJobs;
    private final ProjectStatusPOJO projectStatus;
    private final DataWarehouse dataWarehouse = DataWarehouse.getInstance();
    private final RestHighLevelClient destinationClient;
    private final RestHighLevelClient sourceClient;
    private final int tasksAPIRetriesNumber;

    //TODO setup tasksAPIRetriesNumber in the code
    ReindexProjectExecutor(final ReindexPlanPOJO plan,
                           final long taskRefreshInterval,
                           final int tasksAPIRetriesNumber) throws ClusterConnectionException {
        this.plan = plan;
        this.tasksAPIRetriesNumber = tasksAPIRetriesNumber;
        this.indicesThreadPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(plan.getNumberOfConcurrentProcessedIndices());
        ElasticsearchDbProvider elasticsearchDbProvider = new ElasticsearchDbProvider();
        this.destinationClient = elasticsearchDbProvider.getHighLevelClient(plan.getConnectionSettings().getDestination(), plan.getProjectId());
        this.sourceClient = elasticsearchDbProvider.getHighLevelClient(plan.getConnectionSettings().getDestination(), plan.getProjectId());
        this.reindexJobs = plan.getReindexJobs().values().stream()
                .map(reindexJobPOJO ->
                        new ReindexJobExecutor(plan.getProjectId(),
                                reindexJobPOJO,
                                destinationClient,
                                plan.getThreadsPerIndex(),
                                taskRefreshInterval,
                                tasksAPIRetriesNumber)
                )
                .collect(Collectors.toList());
        this.projectStatus = dataWarehouse.getProjectsStatus().get(plan.getProjectId());
    }

    @Override
    public void run() {
        logger.info("Start to execute plan: " + plan.getProjectId());
        projectStatus.setInActiveProcess(true);
        projectStatus.setStartTime(System.currentTimeMillis());
        ElasticsearchController elasticsearchController = new ElasticsearchController();
        projectStatus.setTotalDocs(plan.getReindexJobs().keySet().stream()
                .mapToLong(index -> {
                    try {
                        long docsCount = elasticsearchController.getIndexDocsCount(plan.getConnectionSettings().getSource(), plan.getProjectId(), index);
                        projectStatus.getReindexJobsStatus().stream()
                                .filter(job -> job.getSourceIndex().equals(index))
                                .forEach(job -> job.setTotalDocs(docsCount));
                        return docsCount;
                    } catch (ClusterConnectionException e) {
                        e.printStackTrace();
                        return 0;
                    }
                })
                .sum());
        if (!plan.isDone()) {
            //Cluster tasks
            boolean clusterTaskResult = true;
            int i = 0;
            while (clusterTaskResult && i < plan.getClusterTasks().size() && !projectStatus.isInterrupted()) {
                if (!plan.getClusterTasks().get(i).isDone()) {
                    IClusterTaskDAO taskForExecution = plan.getClusterTasks().get(i).getRequestDAO();
                    clusterTaskResult = plan.getClusterTasks().get(i).getRequestDAO().execute(
                            destinationClient,
                            projectStatus.getClusterTasksStatus().stream()
                                    .filter(task -> task.getSelfGeneratedTaskId().equals(taskForExecution.getSelfGeneratedTaskId()))
                                    .findFirst().orElse(null)
                    );
                    dataWarehouse.writeStatusToFile(plan.getProjectId());
                }
                i++;
            }

            if (clusterTaskResult && !projectStatus.isInterrupted()) {
                //Reindex tasks
                CompletableFuture<?>[] futures = reindexJobs.stream()
                        .map(job -> CompletableFuture.runAsync(job, indicesThreadPool))
                        .toArray(CompletableFuture[]::new);
                CompletableFuture.allOf(futures).join();
                projectStatus.updateStatus();
            } else {
                projectStatus.setStatus(EProjectStatus.FAILED);
                projectStatus.setFailed(true);
                projectStatus.setDone(true);
            }

            indicesThreadPool.shutdown();
            projectStatus.setInActiveProcess(false);
            projectStatus.setEndTime(System.currentTimeMillis());
            dataWarehouse.writeStatusToFile(plan.getProjectId());
        }
        try {
            if (sourceClient != null) {
                sourceClient.close();
            }
            if (destinationClient != null) {
                destinationClient.close();
            }
        } catch (IOException e) {
            logger.error("Can't close Elasticsearch client in the Project executor after end of project execution. " +
                    "Plan Id: " + plan.getProjectId());
            e.printStackTrace();
        }
    }

    void stop() {
        projectStatus.setInActiveProcess(false);
        projectStatus.setInterrupted(true);
        reindexJobs.stream()
                .filter(job -> job.getReindexJobStatus().isInActiveProcess())
                .forEach(ReindexJobExecutor::stop);
        indicesThreadPool.shutdownNow();
        plan.setEndTime(System.currentTimeMillis());
        projectStatus.setEndTime(System.currentTimeMillis());
        dataWarehouse.writeStatusToFile(plan.getProjectId());
        logger.warn("Stopping exclusion plan: " + plan.getProjectId());
        projectStatus.setDone(true);
        try {
            if (sourceClient != null) {
                sourceClient.close();
            }
            if (destinationClient != null) {
                destinationClient.close();
            }
        } catch (IOException e) {
            logger.error("Can't close Elasticsearch client in the Project executor after stop project execution. " +
                    "Plan Id: " + plan.getProjectId());
            e.printStackTrace();
        }
    }
}