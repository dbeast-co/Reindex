package com.dbeast.reindex.reindex_execution_plan_monitoring;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class ReindexJobStatusPOJO extends AdvancedStatusPOJO {
    @JsonProperty("source_index")
    private String sourceIndex;
    @JsonProperty("reindex_tasks")
    private List<ReindexTaskStatusPOJO> reindexTasks = new LinkedList<>();
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("original_number_of_replicas")
    private int originalNumberOfReplicas;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("original_refresh_interval")
    private int originalRefreshInterval;

    public ReindexJobStatusPOJO(final String sourceIndex) {
        this.sourceIndex = sourceIndex;
    }

    public ReindexJobStatusPOJO() {
    }

    public synchronized void updateTransferredDocsCount() {
        setTransferredDocs(reindexTasks.stream()
                .mapToLong(task -> task.getCreated() + task.getUpdated())
                .sum());
    }

    public int getOriginalNumberOfReplicas() {
        return originalNumberOfReplicas;
    }

    public void setOriginalNumberOfReplicas(int originalNumberOfReplicas) {
        this.originalNumberOfReplicas = originalNumberOfReplicas;
    }

    public int getOriginalRefreshInterval() {
        return originalRefreshInterval;
    }

    public void setOriginalRefreshInterval(int originalRefreshInterval) {
        this.originalRefreshInterval = originalRefreshInterval;
    }

    public boolean isCurrentIndex(String index) {
        return sourceIndex.equals(index);
    }

    public void addReindexTask(final ReindexTaskStatusPOJO reindexTask) {
        reindexTasks.add(reindexTask);
    }

    public List<ReindexTaskStatusPOJO> getReindexTasks() {
        return reindexTasks;
    }

    public void setReindexTasks(List<ReindexTaskStatusPOJO> reindexTasks) {
        this.reindexTasks = reindexTasks;
    }

    public String getSourceIndex() {
        return sourceIndex;
    }

    public void setSourceIndex(String sourceIndex) {
        this.sourceIndex = sourceIndex;
    }

    public void updateStatus() {
        setDone(true);
        List<ReindexTaskStatusPOJO> failedTasks = reindexTasks.stream()
                .filter(ReindexTaskStatusPOJO::isFailed)
                .collect(Collectors.toList());
        if (failedTasks.size() > 0) {
            setFailed(true);
        } else {
            setSucceeded(true);
        }
    }

}
