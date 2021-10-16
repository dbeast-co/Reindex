package com.dbeast.reindex.reindex_execution_plan_monitoring_for_ui.monitoring_page;

import com.dbeast.reindex.reindex_execution_plan_monitoring.ReindexTaskStatusPOJO;

public class ReindexTaskStatusOnFlyForUIMonitoringPagePOJO extends ReindexTaskStatusForUIMonitoringPagePOJO {
    private int executionProgress;
    private long estimatedDocuments;
    private long completedDocuments;

    public ReindexTaskStatusOnFlyForUIMonitoringPagePOJO(ReindexTaskStatusPOJO requestedTask) {
        super(requestedTask);
        this.executionProgress = requestedTask.getExecutionProgress();
        this.estimatedDocuments = requestedTask.getTotal();
        this.completedDocuments = requestedTask.getCreated() + requestedTask.getUpdated();
    }

    public int getExecutionProgress() {
        return executionProgress;
    }

    public void setExecutionProgress(int executionProgress) {
        this.executionProgress = executionProgress;
    }

    public long getEstimatedDocuments() {
        return estimatedDocuments;
    }

    public void setEstimatedDocuments(long estimatedDocuments) {
        this.estimatedDocuments = estimatedDocuments;
    }

    public long getCompletedDocuments() {
        return completedDocuments;
    }

    public void setCompletedDocuments(long completedDocuments) {
        this.completedDocuments = completedDocuments;
    }
}
