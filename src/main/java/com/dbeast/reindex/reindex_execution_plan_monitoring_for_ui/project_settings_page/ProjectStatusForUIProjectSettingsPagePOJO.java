package com.dbeast.reindex.reindex_execution_plan_monitoring_for_ui.project_settings_page;

import com.dbeast.reindex.constants.EProjectStatus;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ProjectStatusForUIProjectSettingsPagePOJO {
    @JsonProperty("execution_progress")
    private int executionProgress;
    @JsonProperty("project_status")
    private EProjectStatus projectStatus = EProjectStatus.NEW;
    @JsonProperty("is_done")
    private boolean isDone;
    @JsonProperty("is_failed")
    private boolean isFailed;
    private boolean isInActiveProcess = false;

    public ProjectStatusForUIProjectSettingsPagePOJO() {
    }

    public ProjectStatusForUIProjectSettingsPagePOJO(int executionProgress, EProjectStatus projectStatus, boolean isDone) {
        this.executionProgress = executionProgress;
        this.projectStatus = projectStatus;
        this.isDone = isDone;

    }

    public ProjectStatusForUIProjectSettingsPagePOJO(int executionProgress,
                                                     EProjectStatus projectStatus,
                                                     boolean isDone,
                                                     boolean isFailed,
                                                     boolean isInActiveProcess) {
        this.executionProgress = executionProgress;
        this.projectStatus = projectStatus;
        this.isDone = isDone;
        this.isFailed = isFailed;
        this.isInActiveProcess = isInActiveProcess;

    }

    public int getExecutionProgress() {
        return executionProgress;
    }

    public void setExecutionProgress(int executionProgress) {
        this.executionProgress = executionProgress;
    }

    public String getProjectStatus() {
        return projectStatus.getStatusDescription();
    }

    public void setProjectStatus(String projectStatus) {
        this.projectStatus = EProjectStatus.getByValue(projectStatus);
    }

    public void setProjectStatus(EProjectStatus projectStatus) {
        this.projectStatus = projectStatus;
    }

    @JsonProperty("is_failed")
    public boolean isFailed() {
        return isFailed;
    }

    public void setFailed(boolean failed) {
        isFailed = failed;
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
}
