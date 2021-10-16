package com.dbeast.reindex.reindex_execution_plan_monitoring;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AdvancedStatusPOJO extends BasicStatusPOJO {

    @JsonProperty("tasks_number")
    private int tasksNumber;
    @JsonProperty("succeeded_tasks")
    private volatile int succeededTasks;
    @JsonProperty("failed_tasks")
    private volatile int failedTasks;
    @JsonProperty("transferred_docs")
    private long transferredDocs;
    @JsonProperty("total_docs")
    private long totalDocs;


    public int getTasksNumber() {
        return tasksNumber;
    }

    public void setTasksNumber(int tasksNumber) {
        this.tasksNumber = tasksNumber;
    }

    public synchronized int getSucceededTasks() {
        return succeededTasks;
    }

    public synchronized void setSucceededTasks(int succeededTasks) {
        this.succeededTasks = succeededTasks;
    }

    public synchronized void incrementSucceededTasks() {
        this.succeededTasks++;
    }

    public synchronized int getFailedTasks() {
        return failedTasks;
    }

    public synchronized void setFailedTasks(int failedTasks) {
        this.failedTasks = failedTasks;
    }

    public synchronized void incrementFailedTasks() {
        this.failedTasks++;
    }

    public synchronized long getTransferredDocs() {
        return transferredDocs;
    }

    public synchronized void setTransferredDocs(long transferredDocs) {
        this.transferredDocs = transferredDocs;
    }

    public synchronized long getTotalDocs() {
        return totalDocs;
    }

    public synchronized void setTotalDocs(long totalDocs) {
        this.totalDocs = totalDocs;
    }

    public synchronized void updateExecutionProgress() {
        this.setExecutionProgress((getSucceededTasks() + getFailedTasks()) * 100 / getTasksNumber());
    }

    @Override
    public String toString() {
        return "AdvancedStatusPOJO{" +
                "tasksNumber=" + tasksNumber +
                ", succeededTasks=" + succeededTasks +
                ", failedTasks=" + failedTasks +
                ", transferredDocs=" + transferredDocs +
                ", totalDocs=" + totalDocs +
                '}';
    }
}
