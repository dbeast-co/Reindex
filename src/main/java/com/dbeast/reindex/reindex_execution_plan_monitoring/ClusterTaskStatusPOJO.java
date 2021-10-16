package com.dbeast.reindex.reindex_execution_plan_monitoring;

import com.dbeast.reindex.reindex_execution_plan_builder.reindex_plan.ClusterTaskPOJO;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ClusterTaskStatusPOJO extends BasicStatusPOJO {
    @JsonProperty("description")
    private String description;
    @JsonProperty("self_generated_task_id")
    private String selfGeneratedTaskId;
    @JsonProperty("exception")
    private String exception;

    public ClusterTaskStatusPOJO() {
    }

    public ClusterTaskStatusPOJO(ClusterTaskPOJO clusterTask) {
        this.description = clusterTask.getDescription();
        this.selfGeneratedTaskId = clusterTask.getSelfGeneratedTaskId();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSelfGeneratedTaskId() {
        return selfGeneratedTaskId;
    }

    public void setSelfGeneratedTaskId(String selfGeneratedTaskId) {
        this.selfGeneratedTaskId = selfGeneratedTaskId;
    }

    public String getException() {
        return exception;
    }

    public void setException(String exception) {
        this.exception = exception;
    }
}
