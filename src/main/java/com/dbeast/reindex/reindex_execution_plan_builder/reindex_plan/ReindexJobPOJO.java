package com.dbeast.reindex.reindex_execution_plan_builder.reindex_plan;

import java.util.LinkedList;
import java.util.List;

public class ReindexJobPOJO {
    private String indexName;
    private List<ReindexTaskPOJO> reindexTasks = new LinkedList<>();
    private boolean isDone = false;
    private boolean isInActiveProcess = false;

    private int originalNumberOfReplicas = 0;
    private String originalRefreshInterval = "-1";

    public ReindexJobPOJO(String indexName) {
        this.indexName = indexName;
    }

    public ReindexJobPOJO() {
    }

    public void addReindexTask(final ReindexTaskPOJO reindexTask) {
        reindexTasks.add(reindexTask);
    }

    public String getIndexName() {
        return indexName;
    }

    public void setIndexName(String indexName) {
        this.indexName = indexName;
    }

    public List<ReindexTaskPOJO> getReindexTasks() {
        return reindexTasks;
    }

    public void setReindexTasks(List<ReindexTaskPOJO> reindexTasks) {
        this.reindexTasks = reindexTasks;
    }

    public boolean isDone() {
        return isDone;
    }

    public void setDone(boolean done) {
        isDone = done;
    }

    public boolean isInActiveProcess() {
        return isInActiveProcess;
    }

    public void setInActiveProcess(boolean inActiveProcess) {
        isInActiveProcess = inActiveProcess;
    }

    public int getOriginalNumberOfReplicas() {
        return originalNumberOfReplicas;
    }

    public void setOriginalNumberOfReplicas(int originalNumberOfReplicas) {
        this.originalNumberOfReplicas = originalNumberOfReplicas;
    }

    public String getOriginalRefreshInterval() {
        return originalRefreshInterval;
    }

    public void setOriginalRefreshInterval(String originalRefreshInterval) {
        this.originalRefreshInterval = originalRefreshInterval;
    }

    @Override
    public String toString() {
        return "ReindexJobPOJO{" +
                "indexName='" + indexName + '\'' +
                ", reindexTasks=" + reindexTasks +
                ", isDone=" + isDone +
                ", isInActiveProcess=" + isInActiveProcess +
                ", originalNumberOfReplicas=" + originalNumberOfReplicas +
                ", originalRefreshInterval='" + originalRefreshInterval + '\'' +
                '}';
    }
}
