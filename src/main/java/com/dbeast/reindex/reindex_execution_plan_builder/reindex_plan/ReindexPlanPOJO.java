package com.dbeast.reindex.reindex_execution_plan_builder.reindex_plan;

import com.dbeast.reindex.project_settings.ConnectionSettingsPOJO;
import com.dbeast.reindex.project_settings.ProjectPOJO;
import com.dbeast.reindex.reindex_execution_plan_monitoring.BasicStatusPOJO;
import com.dbeast.reindex.reindex_execution_plan_monitoring.ProjectStatusPOJO;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ReindexPlanPOJO extends BasicStatusPOJO {
    private final String projectId;
    private final String projectName;
    private final int numberOfConcurrentProcessedIndices;
    private final int threadsPerIndex;
    private final ConnectionSettingsPOJO connectionSettings;
    private final boolean isContinueONFailure;
    private boolean isInActiveProcess = false;
    private List<ClusterTaskPOJO> clusterTasks = new LinkedList<>();
    private Map<String, ReindexJobPOJO> reindexJobs = new HashMap<>();

    public ReindexPlanPOJO(ProjectPOJO project) {
        projectId = project.getProjectId();
        projectName = project.getProjectName();
        connectionSettings = project.getConnectionSettings();
        threadsPerIndex = project.getReindexSettings().getThreadsPerIndex();
        numberOfConcurrentProcessedIndices = project.getReindexSettings().getNumberOfConcurrentProcessedIndices();
        isContinueONFailure = project.getReindexSettings().isContinueOnFailure();
    }

    public ReindexPlanPOJO updateFailedProject(ProjectStatusPOJO projectStatusPOJO) {
        this.setDone(projectStatusPOJO.isDone());
        this.isInActiveProcess = false;
        this.setFailed(projectStatusPOJO.isFailed());
        this.setSucceeded(false);
        this.setInterrupted(projectStatusPOJO.isInterrupted());
        this.setExecutionProgress(projectStatusPOJO.getExecutionProgress());
        this.setStatus(projectStatusPOJO.getStatus());
        this.clusterTasks = new LinkedList<>();

        this.reindexJobs.forEach((index, reindexJob) ->
                projectStatusPOJO.getReindexJobsStatus().stream()
                        .filter(taskStatus -> taskStatus.getSourceIndex().equals(index))
                        .findFirst()
                        .ifPresent(reindexJobStatus -> {
                            reindexJob.setDone(reindexJobStatus.isDone());
                            reindexJob.setInActiveProcess(reindexJobStatus.isInActiveProcess());
                            reindexJob.getReindexTasks().forEach(task -> {
                                reindexJobStatus.getReindexTasks().stream()
                                        .filter(currentTaskStatus -> currentTaskStatus.getReindexParams().equals(task.getReindexParams()))
                                        .findFirst()
                                        .ifPresent(currentTaskStatus -> {
                                            task.setDone(currentTaskStatus.isDone());
                                            task.setInActiveProcess(currentTaskStatus.isInActiveProcess());
                                            task.setFailed(currentTaskStatus.isFailed());
                                            task.setSucceeded(currentTaskStatus.isSucceeded());
                                        });
                            });
                        }));
        return this;
    }

    public int getNumberOfConcurrentProcessedIndices() {
        return numberOfConcurrentProcessedIndices;
    }

    public int getThreadsPerIndex() {
        return threadsPerIndex;
    }

    public boolean isInActiveProcess() {
        return isInActiveProcess;
    }

    public void setInActiveProcess(boolean inActiveProcess) {
        isInActiveProcess = inActiveProcess;
    }

    public List<ClusterTaskPOJO> getClusterTasks() {
        return clusterTasks;
    }

    public void setClusterTasks(List<ClusterTaskPOJO> clusterTasks) {
        this.clusterTasks = clusterTasks;
    }

    public Map<String, ReindexJobPOJO> getReindexJobs() {
        return reindexJobs;
    }

    public void setReindexJobs(Map<String, ReindexJobPOJO> reindexJobs) {
        this.reindexJobs = reindexJobs;
    }

    public String getProjectId() {
        return projectId;
    }

    public String getProjectName() {
        return projectName;
    }

    public ConnectionSettingsPOJO getConnectionSettings() {
        return connectionSettings;
    }

    public boolean isContinueONFailure() {
        return isContinueONFailure;
    }

}

