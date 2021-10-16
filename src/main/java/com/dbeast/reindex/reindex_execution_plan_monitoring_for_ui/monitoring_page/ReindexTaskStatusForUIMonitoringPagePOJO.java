package com.dbeast.reindex.reindex_execution_plan_monitoring_for_ui.monitoring_page;

import com.dbeast.reindex.reindex_execution_plan_monitoring.ReindexTaskStatusPOJO;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.LinkedList;
import java.util.List;

public class ReindexTaskStatusForUIMonitoringPagePOJO {
    @JsonProperty("self_generated_task_id")
    private String selfGeneratedTaskId;
    private String index;
    private String params;
    private boolean isInActiveProcess = false;
    private boolean isFailed = false;
    private boolean isSucceeded = false;
    private boolean isDone = false;
    @JsonProperty("transferred_docs")
    private long transferredDocs;
    @JsonProperty("estimated_docs")
    private long estimatedDocs;
    @JsonProperty("execution_progress")
    private int executionProgress;
    @JsonProperty("failures")
    private List<String> failures = new LinkedList<>();


    public ReindexTaskStatusForUIMonitoringPagePOJO(ReindexTaskStatusPOJO taskStatus) {
        this.selfGeneratedTaskId = taskStatus.getSelfGeneratedTaskId();
        this.index = taskStatus.getSourceIndex();
        this.params = taskStatus.getReindexParams();
        this.isInActiveProcess = taskStatus.isInActiveProcess();
        this.isFailed = taskStatus.isFailed();
        this.isSucceeded = taskStatus.isSucceeded();
        this.isDone = taskStatus.isDone();
        this.transferredDocs = taskStatus.getCreated() + taskStatus.getUpdated();
        this.estimatedDocs = taskStatus.getTotal();
        this.executionProgress = taskStatus.getExecutionProgress();
        this.failures.addAll(taskStatus.getFailures());
        if (taskStatus.getError() != null) {
            this.failures.add(String.valueOf(taskStatus.getError()));
        }
    }

    public ReindexTaskStatusForUIMonitoringPagePOJO() {
    }

    public String getSelfGeneratedTaskId() {
        return selfGeneratedTaskId;
    }

    public void setSelfGeneratedTaskId(String selfGeneratedTaskId) {
        this.selfGeneratedTaskId = selfGeneratedTaskId;
    }

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public String getParams() {
        return params;
    }

    public void setParams(String params) {
        this.params = params;
    }

    @JsonProperty("is_done")
    public boolean isDone() {
        return isDone;
    }

    public void setDone(boolean done) {
        isDone = done;
    }

    @JsonProperty("is_in_active_process")
    public boolean isInActiveProcess() {
        return isInActiveProcess;
    }

    public void setInActiveProcess(boolean inActiveProcess) {
        isInActiveProcess = inActiveProcess;
    }

    @JsonProperty("is_failed")
    public boolean isFailed() {
        return isFailed;
    }

    public void setFailed(boolean failed) {
        isFailed = failed;
    }

    @JsonProperty("is_succeeded")
    public boolean isSucceeded() {
        return isSucceeded;
    }

    public void setSucceeded(boolean succeeded) {
        isSucceeded = succeeded;
    }

    public int getExecutionProgress() {
        return executionProgress;
    }

    public void setExecutionProgress(int executionProgress) {
        this.executionProgress = executionProgress;
    }

    public List<String> getFailures() {
        return failures;
    }

    public void setFailures(List<String> failures) {
        this.failures = failures;
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
}
