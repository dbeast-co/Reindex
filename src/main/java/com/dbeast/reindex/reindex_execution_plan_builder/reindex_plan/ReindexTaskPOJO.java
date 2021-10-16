package com.dbeast.reindex.reindex_execution_plan_builder.reindex_plan;

import org.elasticsearch.index.reindex.ReindexRequest;

import java.util.HashMap;
import java.util.Map;

public class ReindexTaskPOJO {
    private String sourceIndex;
    private String description;
    private String reindexParams;
    private Map<String, Object> reprocessParams = new HashMap<>();
    private String query;
    private boolean isDone = false;
    private boolean isInActiveProcess = false;
    private boolean isFailed = false;
    private boolean isSucceeded = false;
    private ReindexRequest reindexRequest;

    public ReindexTaskPOJO(final String sourceIndexName,
                           final ReindexRequest reindexRequest) {
        this.sourceIndex = sourceIndexName;
        this.reindexRequest = reindexRequest;
        this.reindexRequest.setSourceIndices(sourceIndexName);
        if (reindexRequest.getRemoteInfo() != null) {
            this.query = new String(reindexRequest.getRemoteInfo().getQuery().utf8ToString());
        } else {
            this.query = reindexRequest.getSearchRequest().source().query() != null ?
                    reindexRequest.getSearchRequest().source().query().toString() : "";
        }
    }

    public ReindexTaskPOJO(String sourceIndexName) {
        this.sourceIndex = sourceIndexName;
    }

    public ReindexTaskPOJO() {
    }

    public Map<String, Object> getReprocessParams() {
        return reprocessParams;
    }

    public void setReprocessParams(Map<String, Object> reprocessParams) {
        this.reprocessParams = reprocessParams;
    }

    public void addReprocessParam(final String key, final Object value) {
        reprocessParams.put(key, value);
    }

    public String getReindexParams() {
        return reindexParams;
    }

    public void setReindexParams(String params) {
        this.reindexParams = params;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getSourceIndex() {
        return sourceIndex;
    }

    public void setSourceIndex(String sourceIndex) {
        this.sourceIndex = sourceIndex;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public boolean isFailed() {
        return isFailed;
    }

    public void setFailed(boolean failed) {
        isFailed = failed;
    }

    public boolean isSucceeded() {
        return isSucceeded;
    }

    public void setSucceeded(boolean succeeded) {
        isSucceeded = succeeded;
    }

    public ReindexRequest getReindexRequest() {
        return reindexRequest;
    }

    public void setReindexRequest(ReindexRequest reindexRequest) {
        this.reindexRequest = reindexRequest;
        this.description = reindexRequest.getDescription();
        this.reindexRequest = reindexRequest;
        this.reindexRequest.setSourceIndices(this.sourceIndex);
        if (reindexRequest.getRemoteInfo() != null) {
            this.query = new String(reindexRequest.getRemoteInfo().getQuery().utf8ToString());
        } else {
            this.query = reindexRequest.getSearchRequest().source().query() != null ?
                    reindexRequest.getSearchRequest().source().query().toString() : "";
        }
    }

    @Override
    public String toString() {
        return "ReindexTaskPOJO{" +
                ", description='" + description + '\'' +
                ", isDone=" + isDone +
                ", isInActiveProcess=" + isInActiveProcess +
                ", isFailed=" + isFailed +
                ", isSucceeded=" + isSucceeded +
                '}';
    }
}
