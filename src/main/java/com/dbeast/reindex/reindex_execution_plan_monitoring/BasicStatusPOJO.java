package com.dbeast.reindex.reindex_execution_plan_monitoring;

import com.dbeast.reindex.constants.EProjectStatus;
import com.dbeast.reindex.utils.GeneralUtils;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.text.SimpleDateFormat;

public class BasicStatusPOJO {

    private boolean isDone = false;
    private boolean isInActiveProcess = false;
    private boolean isFailed = false;
    private boolean isSucceeded = false;
    private boolean isInterrupted = false;
    @JsonProperty("execution_progress")
    private int executionProgress;
    @JsonProperty("status")
    private EProjectStatus status = EProjectStatus.NEW;
    @JsonProperty("start_time")
    private long startTime;
    @JsonProperty("start_time_string")
    private String startTimeString;
    @JsonProperty("end_time")
    private long endTime;
    @JsonProperty("end_time_string")
    private String endTimeString;

    public int getExecutionProgress() {
        return executionProgress;
    }

    public synchronized void setExecutionProgress(int executionProgress) {
        this.executionProgress = executionProgress;
    }

    public EProjectStatus getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = EProjectStatus.getByValue(status);
    }

    public void setStatus(EProjectStatus status) {
        this.status = status;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        this.startTimeString = GeneralUtils.convertLongToDateString(startTime, dateFormat);
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        this.endTimeString = GeneralUtils.convertLongToDateString(endTime, dateFormat);
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
        if (inActiveProcess) {
            this.status = EProjectStatus.ON_FLY;
        }
    }

    @JsonProperty("is_failed")
    public boolean isFailed() {
        return isFailed;
    }

    public synchronized void setFailed(boolean failed) {
        isFailed = failed;
        if (failed) {
            status = EProjectStatus.FAILED;
        }
    }

    @JsonProperty("is_succeeded")
    public boolean isSucceeded() {
        return isSucceeded;
    }

    public void setSucceeded(boolean succeeded) {
        isSucceeded = succeeded;
        if (succeeded) {
            this.status = EProjectStatus.SUCCEEDED;
        }
    }

    @JsonProperty("is_interrupted")
    public boolean isInterrupted() {
        return isInterrupted;
    }

    public void setInterrupted(boolean interrupted) {
        isInterrupted = interrupted;
        if (interrupted) {
            this.status = EProjectStatus.STOPPED;
        }
    }

    public String getStartTimeString() {
        return startTimeString;
    }

    public String getEndTimeString() {
        return endTimeString;
    }

    public void setEndTimeString(String endTimeString) {
        this.endTimeString = endTimeString;
    }

    public void setStartTimeString(String startTimeString) {
        this.startTimeString = startTimeString;
    }

    @Override
    public String toString() {
        return "BasicStatusPOJO{" +
                ", isDone=" + isDone +
                ", isInActiveProcess=" + isInActiveProcess +
                ", isFailed=" + isFailed +
                ", isSucceeded=" + isSucceeded +
                '}';
    }

}
