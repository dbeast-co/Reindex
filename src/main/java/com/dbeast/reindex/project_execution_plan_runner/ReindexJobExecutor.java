package com.dbeast.reindex.project_execution_plan_runner;

import com.dbeast.reindex.constants.EProjectStatus;
import com.dbeast.reindex.data_warehouse.DataWarehouse;
import com.dbeast.reindex.elasticsearch.ElasticsearchController;
import com.dbeast.reindex.reindex_execution_plan_builder.reindex_plan.ReindexJobPOJO;
import com.dbeast.reindex.reindex_execution_plan_monitoring.ProjectStatusPOJO;
import com.dbeast.reindex.reindex_execution_plan_monitoring.ReindexJobStatusPOJO;
import com.dbeast.reindex.reindex_execution_plan_monitoring.ReindexTaskStatusPOJO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.tasks.CancelTasksRequest;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

public class ReindexJobExecutor implements Runnable {
    private static final Logger logger = LogManager.getLogger();

    private final String projectId;
    private final ReindexJobPOJO reindexJob;
    private final RestHighLevelClient client;
    private final long taskRefreshInterval;
    private final ReindexJobStatusPOJO reindexJobStatus;
    private final ThreadPoolExecutor reindexTasksThreadPool;
    private final DataWarehouse dataWarehouse = DataWarehouse.getInstance();
    private final int tasksAPIRetriesNumber;
    private final ProjectStatusPOJO reindexProjectStatus;
    private final ElasticsearchController elasticsearchController = new ElasticsearchController();

    public ReindexJobExecutor(final String projectId,
                              final ReindexJobPOJO reindexJob,
                              final RestHighLevelClient client,
                              final int threadsPerIndex,
                              final long taskRefreshInterval,
                              final int tasksAPIRetriesNumber) {
        this.taskRefreshInterval = taskRefreshInterval;
        this.projectId = projectId;
        this.reindexJob = reindexJob;
        this.client = client;
        this.tasksAPIRetriesNumber = tasksAPIRetriesNumber;
        reindexProjectStatus = dataWarehouse.getProjectsStatus().get(projectId);
        reindexJobStatus = reindexProjectStatus.getReindexJobsStatus().stream()
                .filter(job -> job.isCurrentIndex(reindexJob.getIndexName()))
                .collect(Collectors.toList())
                .get(0);
        reindexTasksThreadPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(threadsPerIndex);
    }

    @Override
    public void run() {
        logger.info("Start to execute job for index: " + reindexJob.getIndexName());
        reindexJobStatus.setInActiveProcess(true);
        reindexJobStatus.setStartTime(System.currentTimeMillis());
        if (reindexJobStatus.getTotalDocs() > 0) {
            //TODO change index number of replica
//            getAndUpdateOriginalIndexSettings();
            CompletableFuture<?>[] futures = reindexJob.getReindexTasks().stream()
                    .filter(task -> !task.isDone())
                    .map(task -> CompletableFuture.runAsync(new ReindexTaskExecutor(projectId,
                                    task,
                                    client,
                                    taskRefreshInterval,
                                    tasksAPIRetriesNumber),
                            reindexTasksThreadPool))
                    .toArray(CompletableFuture[]::new);
            CompletableFuture.allOf(futures).join();
            returnOriginalIndexSettings();
            if (!reindexJobStatus.getStatus().equals(EProjectStatus.STOPPED)) {
                reindexJobStatus.updateStatus();
            }
        } else {
            reindexJobStatus.setDone(true);
            reindexJobStatus.setFailed(false);
            reindexJobStatus.setExecutionProgress(100);
            reindexProjectStatus.incrementSucceededTasks();
            reindexJobStatus.setStatus(EProjectStatus.SUCCEEDED);
        }

// Return original index settings
        reindexJobStatus.setInActiveProcess(false);
        reindexTasksThreadPool.shutdown();
        reindexJobStatus.setEndTime(System.currentTimeMillis());
        dataWarehouse.writeStatusToFile(projectId);
    }

    private void getAndUpdateOriginalIndexSettings() {
        if (dataWarehouse.getProjectsMap().get(projectId).getReindexSettings().isAddIndexPrefix() ||
                dataWarehouse.getProjectsMap().get(projectId).getReindexSettings().isAddIndexSuffix() ||
                dataWarehouse.getProjectsMap().get(projectId).getReindexSettings().isRemoveIndexSuffix() ||
                dataWarehouse.getProjectsMap().get(projectId).getReindexSettings().isMergeToOneIndex()) {
            boolean creationResult = elasticsearchController.createIndex(client,
                    reindexJob.getReindexTasks().get(0).getReindexRequest().getDestination().index());
            if (creationResult) {
                int originalNumberOfReplicas = elasticsearchController.getNumberOfReplicasFromIndex(client,
                        reindexJob.getReindexTasks().get(0).getReindexRequest().getDescription());
                if (originalNumberOfReplicas > 0) {
                    reindexJobStatus.setOriginalNumberOfReplicas(originalNumberOfReplicas);
                    reindexJob.setOriginalNumberOfReplicas(originalNumberOfReplicas);
                    elasticsearchController.updateNumberOfReplicas(client,
                            reindexJob.getReindexTasks().get(0).getReindexRequest().getDescription(),
                            0);
                }
            }

        }
    }

    private void returnOriginalIndexSettings() {
        if (dataWarehouse.getProjectsMap().get(projectId).getReindexSettings().isAddIndexPrefix() ||
                dataWarehouse.getProjectsMap().get(projectId).getReindexSettings().isAddIndexSuffix() ||
                dataWarehouse.getProjectsMap().get(projectId).getReindexSettings().isRemoveIndexSuffix() ||
                dataWarehouse.getProjectsMap().get(projectId).getReindexSettings().isMergeToOneIndex()) {
            if (reindexJob.getOriginalNumberOfReplicas() > 0) {
                elasticsearchController.updateNumberOfReplicas(client,
                        reindexJob.getReindexTasks().get(0).getReindexRequest().getDestination().index(),
                        0);
            }
        }
    }

    public void stop() {
        reindexJobStatus.setInActiveProcess(false);
        reindexJobStatus.setInterrupted(true);
        reindexJobStatus.getReindexTasks().stream()
                .filter(ReindexTaskStatusPOJO::isInActiveProcess)
                .forEach(task -> {
                    task.setInActiveProcess(false);
                    task.setStatus(EProjectStatus.STOPPED);
                    cancelTask(task.getTaskId());
                });
        reindexTasksThreadPool.shutdownNow();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        dataWarehouse.writeStatusToFile(projectId);
        logger.warn("The job for index: " + reindexJob.getIndexName() + " was interrupted");
    }


    public ReindexJobStatusPOJO getReindexJobStatus() {
        return reindexJobStatus;
    }

    public void cancelTask(final String taskId) {
        try {
            logger.info("Canceling the task: " + taskId);
            CancelTasksRequest byTaskIdRequest = new org.elasticsearch.client.tasks.CancelTasksRequest.Builder()
                    .withTaskId(new org.elasticsearch.client.tasks.TaskId(taskId))
                    .build();
            client.tasks().cancel(byTaskIdRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            logger.warn("The error in the stopping the task" + e);
        }
    }
}
