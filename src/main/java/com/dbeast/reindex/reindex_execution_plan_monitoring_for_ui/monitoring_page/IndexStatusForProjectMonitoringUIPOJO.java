package com.dbeast.reindex.reindex_execution_plan_monitoring_for_ui.monitoring_page;

import com.dbeast.reindex.constants.EProjectStatus;
import com.dbeast.reindex.reindex_execution_plan_monitoring.ReindexJobStatusPOJO;
import com.fasterxml.jackson.annotation.JsonProperty;

public class IndexStatusForProjectMonitoringUIPOJO {
    @JsonProperty("index_name")
    private String indexName;
    @JsonProperty("execution_progress")
    private int executionProgress;
    @JsonProperty("index_status")
    private EProjectStatus indexStatus;
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

    public IndexStatusForProjectMonitoringUIPOJO(ReindexJobStatusPOJO jobStatus) {
        this.indexName = jobStatus.getSourceIndex();
        this.indexStatus =jobStatus.getStatus();
        this.executionProgress = jobStatus.getExecutionProgress();
        this.startTime = jobStatus.getStartTimeString();
        this.endTime = jobStatus.getEndTimeString();
        this.tasksNumber = jobStatus.getTasksNumber();
        this.succeededTasks = jobStatus.getSucceededTasks();
        this.failedTasks = jobStatus.getFailedTasks();
        this.transferredDocs = jobStatus.getTransferredDocs();
        this.estimatedDocs = jobStatus.getTotalDocs();
    }

    public IndexStatusForProjectMonitoringUIPOJO() {
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

    public String getIndexName() {
        return indexName;
    }

    public void setIndexName(String indexName) {
        this.indexName = indexName;
    }

    public int getExecutionProgress() {
        return executionProgress;
    }

    public void setExecutionProgress(int executionProgress) {
        this.executionProgress = executionProgress;
    }

    public EProjectStatus getIndexStatus() {
        return indexStatus;
    }

    public void setIndexStatus(EProjectStatus indexStatus) {
        this.indexStatus = indexStatus;
    }
}
