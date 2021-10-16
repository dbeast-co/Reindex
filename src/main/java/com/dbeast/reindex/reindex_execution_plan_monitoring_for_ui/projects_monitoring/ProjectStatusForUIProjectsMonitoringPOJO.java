package com.dbeast.reindex.reindex_execution_plan_monitoring_for_ui.projects_monitoring;

import com.dbeast.reindex.constants.EProjectStatus;
import com.dbeast.reindex.reindex_execution_plan_monitoring.ProjectStatusPOJO;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ProjectStatusForUIProjectsMonitoringPOJO {
    @JsonProperty("project_id")
    private String projectId;
    @JsonProperty("project_name")
    private String projectName;
    @JsonProperty("execution_progress")
    private int executionProgress;
    @JsonProperty("project_status")
    private EProjectStatus projectStatus = EProjectStatus.NEW;
    @JsonProperty("start_time")
    private String startTime;
    @JsonProperty("end_time")
    private String endTime;
    @JsonProperty("tasks_number")
    private int tasksNumber;
    @JsonProperty("succeeded_tasks")
    private int succeededTasks;
    @JsonProperty("failed_tasks")
    private int failedTasks;
    @JsonProperty("transferred_docs")
    private long transferredDocs;
    @JsonProperty("estimated_docs")
    private long estimatedDocs;
    @JsonProperty("progress")
    private int progress;

    public ProjectStatusForUIProjectsMonitoringPOJO(final ProjectStatusPOJO projectStatus) {
        this.projectId = projectStatus.getProjectId();
        this.projectName = projectStatus.getProjectName();
        this.projectStatus = projectStatus.getStatus();
        this.executionProgress = projectStatus.getExecutionProgress();
        this.startTime = projectStatus.getStartTimeString();
        this.endTime = projectStatus.getEndTimeString();
        this.tasksNumber = projectStatus.getTasksNumber();
        this.succeededTasks = projectStatus.getSucceededTasks();
        this.failedTasks = projectStatus.getFailedTasks();
        this.transferredDocs = projectStatus.getTransferredDocs();
        this.estimatedDocs = projectStatus.getTotalDocs();
        this.progress = projectStatus.getExecutionProgress();
    }

    public ProjectStatusForUIProjectsMonitoringPOJO() {
    }

    public int getTasksNumber() {
        return tasksNumber;
    }

    public void setTasksNumber(int tasksNumber) {
        this.tasksNumber = tasksNumber;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public int getSucceededTasks() {
        return succeededTasks;
    }

    public void setSucceededTasks(int succeededTasks) {
        this.succeededTasks = succeededTasks;
    }

    public int getFailedTasks() {
        return failedTasks;
    }

    public void setFailedTasks(int failedTasks) {
        this.failedTasks = failedTasks;
    }

    public long getTransferredDocs() {
        return transferredDocs;
    }

    public void setTransferredDocs(long transferredDocs) {
        this.transferredDocs = transferredDocs;
    }

    public long getEstimatedDocs() {
        return estimatedDocs;
    }

    public void setEstimatedDocs(long estimatedDocs) {
        this.estimatedDocs = estimatedDocs;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public int getExecutionProgress() {
        return executionProgress;
    }

    public void setExecutionProgress(int executionProgress) {
        this.executionProgress = executionProgress;
    }

    public EProjectStatus getProjectStatus() {
        return projectStatus;
    }

    public void setProjectStatus(EProjectStatus projectStatus) {
        this.projectStatus = projectStatus;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }
}
