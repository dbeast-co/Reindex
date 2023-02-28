package com.dbeast.reindex.project_execution_plan_runner;

import com.dbeast.reindex.data_warehouse.DataWarehouse;
import com.dbeast.reindex.elasticsearch.ElasticsearchController;
import com.dbeast.reindex.reindex_execution_plan_builder.reindex_plan.ReindexJobPOJO;
import com.dbeast.reindex.reindex_execution_plan_builder.reindex_plan.ReindexPlanPOJO;
import com.dbeast.reindex.reindex_execution_plan_builder.reindex_plan.ReindexTaskPOJO;
import com.dbeast.reindex.reindex_execution_plan_monitoring.ProjectStatusPOJO;
import com.dbeast.reindex.reindex_execution_plan_monitoring.ReindexJobStatusPOJO;
import com.dbeast.reindex.reindex_execution_plan_monitoring.ReindexTaskStatusPOJO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.index.OneMergeWrappingMergePolicy;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.client.*;
import org.elasticsearch.client.tasks.GetTaskRequest;
import org.elasticsearch.client.tasks.GetTaskResponse;
import org.elasticsearch.client.tasks.TaskSubmissionResponse;
import org.elasticsearch.common.breaker.CircuitBreakingException;

import java.io.IOException;
import java.util.stream.Collectors;

public class ReindexTaskExecutor implements Runnable {
    private static final Logger logger = LogManager.getLogger();

    private final ReindexTaskPOJO task;
    private final RestHighLevelClient client;
    private final String projectId;
    private final RestClient lowLevelClient;
    private final long taskRefreshInterval;
    private final ReindexTaskStatusPOJO reindexTaskStatus;
    private final ReindexJobStatusPOJO reindexJobStatus;
    private final ProjectStatusPOJO reindexProjectStatus;
    private final DataWarehouse dataWarehouse = DataWarehouse.getInstance();
    private final boolean isContinueONFailure;
    private final ReindexPlanPOJO reindexProjectPlan;
    private final ReindexTaskPOJO reindexTaskPlan;
    private final int taskRetries;
    private final ElasticsearchController elasticsearchController = new ElasticsearchController();

    public ReindexTaskExecutor(final String projectId,
                               final ReindexTaskPOJO task,
                               final RestHighLevelClient client,
                               final long taskRefreshInterval,
                               final int taskRetries) {
        this.taskRefreshInterval = taskRefreshInterval;
        this.projectId = projectId;
        this.task = task;
        this.client = client;
        this.lowLevelClient = client.getLowLevelClient();
        this.taskRetries = taskRetries;

        reindexProjectStatus = dataWarehouse.getProjectsStatus().get(projectId);
        reindexJobStatus = reindexProjectStatus.getReindexJobsStatus().stream()
                .filter(job -> job.isCurrentIndex(task.getSourceIndex()))
                .collect(Collectors.toList())
                .get(0);
        reindexTaskStatus = reindexJobStatus.getReindexTasks().stream()
                .filter(taskStatus -> (taskStatus.getDescription().equals(task.getDescription())) &&
                        taskStatus.getQuery().equals(task.getQuery()))
                .collect(Collectors.toList())
                .get(0);
        isContinueONFailure = dataWarehouse.getProjectsReindexPlanMap().get(projectId).isContinueONFailure();

        reindexProjectPlan = dataWarehouse.getProjectsReindexPlanMap().get(projectId);
        ReindexJobPOJO reindexJobPlan = reindexProjectPlan.getReindexJobs().values().stream()
                .filter(job -> job.getIndexName().equals(task.getSourceIndex()))
                .collect(Collectors.toList())
                .get(0);
        reindexTaskPlan = reindexJobPlan.getReindexTasks().stream()
                .filter(currentTusk -> (currentTusk.getDescription().equals(task.getDescription())) &&
                        currentTusk.getQuery().equals(task.getQuery()))
                .collect(Collectors.toList())
                .get(0);
    }

    @Override
    public void run() {
        logger.info("Start to execute Task for index: " + task.getSourceIndex() + " Reindex part: " + task.getQuery());
        try {
            reindexTaskStatus.setInActiveProcess(true);

            TaskSubmissionResponse reindexSubmission = client
                    .submitReindexTask(task.getReindexRequest(), RequestOptions.DEFAULT);

            String taskId = reindexSubmission.getTask();
            reindexTaskStatus.setTaskId(taskId);

            String[] taskForRequest = taskId.split(":");
            GetTaskRequest taskRequest = new GetTaskRequest(taskForRequest[0], Long.parseLong(taskForRequest[1]));
            GetTaskResponse response;
            int numberOfRetries = 0;

            do {
                Thread.sleep(taskRefreshInterval * 1000);
                try {
                    response = client.tasks().get(taskRequest, RequestOptions.DEFAULT).orElse(null);
                    if (response != null && response.getTaskInfo() != null) {
                        synchronized (this) {
                            reindexTaskStatus.updateStatus(response.getTaskInfo());
                            reindexTaskStatus.setDone(response.isCompleted());
                            this.task.setDone(response.isCompleted());
                            this.reindexTaskPlan.setDone(response.isCompleted());
                        }
                    } else {
                        reindexTaskStatus.setDone(true);
                        this.reindexTaskPlan.setDone(true);
                        break;
                    }
                    synchronized (this) {
                        reindexJobStatus.updateTransferredDocsCount();
                        reindexProjectStatus.updateTransferredDocsCount();
                        dataWarehouse.writeStatusToFile(projectId);
                    }
                    numberOfRetries = 0;
                } catch (IOException | ElasticsearchException e) {
                    if (numberOfRetries < taskRetries) {
                        logger.warn("There is an error in the Task execution loop in the project: " + projectId + ". " +
                                "Task description: " + task.getDescription() + ". " +
                                "We have " + (taskRetries - numberOfRetries) + "  more retries. " +
                                "Error:" + e.getCause());
                        numberOfRetries++;
                    } else {
                        throw e;
                    }
                }
            } while (!reindexTaskStatus.isDone() && numberOfRetries < taskRetries);
            boolean isError = false;
            Response lowResponse = null;
            numberOfRetries = 0;
            do {
                Thread.sleep(taskRefreshInterval * 1000);
                try {
                    lowResponse = lowLevelClient.performRequest(new Request("GET", "_tasks/" + taskId));
                } catch (IOException | ElasticsearchException e) {
                    if (numberOfRetries < taskRetries) {
                        logger.warn("There is an error in the Task execution loop in the project: " + projectId + ". " +
                                "Task description: " + task.getDescription() + ". " +
                                "We have " + (taskRetries - numberOfRetries) + "  more retries. " +
                                "Error:" + e.getCause());
                        numberOfRetries++;
                        isError = true;
                    } else {
                        throw e;
                    }
                }
            } while (isError);
            ObjectMapper mapper = new ObjectMapper();
            JsonNode afterTaskEndResponse = mapper.readTree(EntityUtils.toString(lowResponse.getEntity()));
            synchronized (this) {
                reindexTaskStatus.updateStatus(afterTaskEndResponse);
                reindexTaskStatus.updateStatuses();
                if (reindexTaskStatus.isSucceeded()) {
                    reindexJobStatus.incrementSucceededTasks();
                    reindexProjectStatus.incrementSucceededTasks();
                    this.reindexTaskPlan.setSucceeded(true);
                } else {
                    reindexJobStatus.incrementFailedTasks();
                    reindexProjectStatus.incrementFailedTasks();
                    this.reindexTaskPlan.setFailed(true);
                    this.reindexProjectPlan.setFailed(true);
                    stopIfFailure();
                }
                reindexJobStatus.updateTransferredDocsCount();
                reindexProjectStatus.updateTransferredDocsCount();
                reindexJobStatus.updateExecutionProgress();
                reindexProjectStatus.updateExecutionProgress();
            }
        } catch (IOException | ElasticsearchException e) {
            reindexTaskStatus.setInActiveProcess(false);
            reindexTaskStatus.setFailed(true);
            reindexTaskStatus.setDone(true);
            this.reindexTaskPlan.setFailed(true);
            this.reindexTaskPlan.setDone(true);
            this.reindexTaskPlan.setInActiveProcess(false);
            this.reindexProjectPlan.setFailed(true);
            reindexTaskStatus.setError(e.getCause());

            synchronized (this) {
                reindexJobStatus.incrementFailedTasks();
                reindexProjectStatus.incrementFailedTasks();
                this.reindexTaskPlan.setFailed(true);
                this.reindexProjectPlan.setFailed(true);
                stopIfFailure();
            }

            logger.warn("There is an error in the Task execution. Error:" + e.getCause());
            logger.warn("Task execution of task for project: " + projectId + " was interrupted. Task description: " +
                    task.getDescription()
                    + " IMPORTANT! MAY BE THE TASK INSIDE THE ELASTICSEARCH DOESN'T STOPPED");
            stopIfFailure();
        } catch (InterruptedException e) {
            if (reindexJobStatus.isInActiveProcess() && e.getCause() != null) {
                reindexTaskStatus.setInActiveProcess(false);
                reindexTaskStatus.setInterrupted(true);
                reindexTaskStatus.setDone(false);
                this.reindexTaskPlan.setInActiveProcess(false);
                this.reindexTaskPlan.setDone(false);
                reindexTaskStatus.setError(e.getCause());

                synchronized (this) {
                    reindexJobStatus.incrementFailedTasks();
                    reindexProjectStatus.incrementFailedTasks();
                    this.reindexTaskPlan.setFailed(true);
                    this.reindexProjectPlan.setFailed(true);
                    stopIfFailure();
                }

                logger.warn("There is an error in the Task execution. Error:" + e.getCause());
                logger.warn("Task execution of task for project: " + projectId + " was interrupted. Task description: " + task.getDescription()
                        + " IMPORTANT! THE TASK INSIDE ELASTICSEARCH DOESN'T STOPPED");
                stopIfFailure();
            } else {
                logger.warn("Task: " + task.getDescription() + " of the project with id: " + projectId + " was interrupted by user");
            }
        } finally {
            dataWarehouse.writeStatusToFile(projectId);
        }
    }

    private void stopIfFailure() {
        if (!isContinueONFailure) {
            logger.error("There is a failure in the reindex of the index: " + reindexTaskStatus.getSourceIndex() +
                    " The reindex process will be terminated! ");
            dataWarehouse.getProjectsExecutors().get(projectId).stop();
        }
    }

}
