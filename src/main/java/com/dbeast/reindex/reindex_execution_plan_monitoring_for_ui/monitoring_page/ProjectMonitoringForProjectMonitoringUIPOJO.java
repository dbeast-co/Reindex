package com.dbeast.reindex.reindex_execution_plan_monitoring_for_ui.monitoring_page;

import com.dbeast.reindex.constants.EProjectStatus;
import com.dbeast.reindex.reindex_execution_plan_monitoring.ProjectStatusPOJO;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.stream.Collectors;

public class ProjectMonitoringForProjectMonitoringUIPOJO {
    @JsonProperty("is_done")
    private boolean isDone;
    @JsonProperty("project_id")
    private String projectId;
    @JsonProperty("project_name")
    private String projectName;
    private EProjectStatus status = EProjectStatus.NEW;
    @JsonProperty("execution_progress")
    private int executionProgress;
    @JsonProperty("total_tasks")
    private int totalTasks;
    @JsonProperty("waiting_tasks")
    private int waitingTasks;
    @JsonProperty("succeeded_tasks")
    private int succeededTasks;
    @JsonProperty("failed_tasks")
    private int failedTasks;
    @JsonProperty("transferred_docs")
    private long transferredDocs;
    @JsonProperty("estimated_docs")
    private long estimatedDocs;
    @JsonProperty("index_status")
    private List<IndexStatusForProjectMonitoringUIPOJO> indexStatus;
    @JsonProperty("on_fly_tasks_status")
    private List<ReindexTaskStatusForUIMonitoringPagePOJO> onFlyTasksStatus;
    @JsonProperty("failed_tasks_status")
    private List<ReindexTaskStatusForUIMonitoringPagePOJO> failedTasksStatus;

    public ProjectMonitoringForProjectMonitoringUIPOJO(final ProjectStatusPOJO projectStatus) {
        this.isDone = projectStatus.isDone();
        this.projectId = projectStatus.getProjectId();
        this.projectName = projectStatus.getProjectName();
        this.executionProgress = projectStatus.getExecutionProgress();
        this.totalTasks = projectStatus.getTasksNumber();
        this.waitingTasks = projectStatus.getTasksNumber() - projectStatus.getSucceededTasks() - projectStatus.getFailedTasks();
        this.succeededTasks = projectStatus.getSucceededTasks();
        this.failedTasks = projectStatus.getFailedTasks();
        this.estimatedDocs = projectStatus.getTotalDocs();
        this.transferredDocs = projectStatus.getTransferredDocs();
        this.status = projectStatus.getStatus();
        List<ReindexTaskStatusForUIMonitoringPagePOJO> allTasks = projectStatus.generateTaskStatusListForUIMonitoringPage();
        failedTasksStatus = allTasks.stream()
                .filter(ReindexTaskStatusForUIMonitoringPagePOJO::isFailed)
                .collect(Collectors.toList());
        onFlyTasksStatus = allTasks.stream()
                .filter(ReindexTaskStatusForUIMonitoringPagePOJO::isInActiveProcess)
                .collect(Collectors.toList());

        indexStatus = projectStatus.getReindexJobsStatus().stream()
                .map(IndexStatusForProjectMonitoringUIPOJO::new)
                .collect(Collectors.toList());
    }

    public ProjectMonitoringForProjectMonitoringUIPOJO(final String projectId) {
        this.projectId = projectId;
    }

    @JsonProperty("is_done")
    public boolean isDone() {
        return isDone;
    }

    public void setDone(boolean done) {
        isDone = done;
    }

    public EProjectStatus getStatus() {
        return status;
    }

    public void setStatus(EProjectStatus status) {
        this.status = status;
    }

    public int getExecutionProgress() {
        return executionProgress;
    }

    public void setExecutionProgress(int executionProgress) {
        this.executionProgress = executionProgress;
    }

    public int getTotalTasks() {
        return totalTasks;
    }

    public void setTotalTasks(int totalTasks) {
        this.totalTasks = totalTasks;
    }

    public int getWaitingTasks() {
        return waitingTasks;
    }

    public void setWaitingTasks(int waitingTasks) {
        this.waitingTasks = waitingTasks;
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

    public List<IndexStatusForProjectMonitoringUIPOJO> getIndexStatus() {
        return indexStatus;
    }

    public void setIndexStatus(List<IndexStatusForProjectMonitoringUIPOJO> indexStatus) {
        this.indexStatus = indexStatus;
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

    public List<ReindexTaskStatusForUIMonitoringPagePOJO> getOnFlyTasksStatus() {
        return onFlyTasksStatus;
    }

    public void setOnFlyTasksStatus(List<ReindexTaskStatusForUIMonitoringPagePOJO> onFlyTasksStatus) {
        this.onFlyTasksStatus = onFlyTasksStatus;
    }

    public List<ReindexTaskStatusForUIMonitoringPagePOJO> getFailedTasksStatus() {
        return failedTasksStatus;
    }

    public void setFailedTasksStatus(List<ReindexTaskStatusForUIMonitoringPagePOJO> failedTasksStatus) {
        this.failedTasksStatus = failedTasksStatus;
    }
}
