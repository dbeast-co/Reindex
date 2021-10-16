package com.dbeast.reindex.reindex_execution_plan_monitoring;

import com.dbeast.reindex.utils.GeneralUtils;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.elasticsearch.tasks.RawTaskStatus;
import org.elasticsearch.tasks.Task;
import org.elasticsearch.tasks.TaskInfo;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ReindexTaskStatusPOJO extends BasicStatusPOJO {
    @JsonProperty("source_index")
    private String sourceIndex;
    @JsonProperty("description")
    private String description;
    @JsonProperty("reindex_params")
    private String reindexParams;
    @JsonProperty("query")
    private String query;
    @JsonProperty("total")
    private long total;
    @JsonProperty("created")
    private long created;
    @JsonProperty("updated")
    private long updated;
    @JsonProperty("deleted")
    private long deleted;
    @JsonProperty("batches")
    private int batches;
    @JsonProperty("failures")
    private List<String> failures = new LinkedList<>();
    @JsonProperty("error")
    private Object error;
    @JsonProperty("running_time_in_nanos")
    private long runningTimeNanos;
    @JsonProperty("version_conflicts")
    private long versionConflicts;
    @JsonProperty("noops")
    private long noops;
    @JsonProperty("bulk_retries")
    private long bulkRetries;
    @JsonProperty("search_retries")
    private long searchRetries;
    @JsonProperty("throttled")
    private long throttled;
    @JsonProperty("requests_per_seconds")
    private float requestsPerSecond;
    @JsonProperty("throttled_units_millis")
    private long throttledUntilMillis;
    @JsonProperty("self_generated_task_id")
    private String selfGeneratedTaskId = GeneralUtils.generateNewUID();
    @JsonProperty("tasks_id")
    private String taskId;
    @JsonProperty("reprocess_params")
    private Map<String, Object> reprocessParams = new HashMap<>();


    public ReindexTaskStatusPOJO(final String taskId,
                                 final String sourceIndex,
                                 final String description,
                                 final String query,
                                 final String reindexParams) {
        this.taskId = taskId;
        this.sourceIndex = sourceIndex;
        this.description = description;
        this.query = query;
        this.reindexParams = reindexParams;
    }

    public ReindexTaskStatusPOJO(final String taskId,
                                 final String sourceIndex,
                                 final String description,
                                 final String query,
                                 final String reindexParams,
                                 final Map<String, Object> reprocessParams) {
        this.taskId = taskId;
        this.sourceIndex = sourceIndex;
        this.description = description;
        this.query = query;
        this.reindexParams = reindexParams;
        this.reprocessParams = reprocessParams;
    }

    public ReindexTaskStatusPOJO() {
    }

    public void updateStatus(final TaskInfo taskInfo) {
        Task.Status status = taskInfo.getStatus();
        Map<String, Object> taskStatus = ((RawTaskStatus) status).toMap();
        this.setStartTime(taskInfo.getStartTime());
        this.runningTimeNanos = taskInfo.getRunningTimeNanos();
        this.total = Long.parseLong(taskStatus.get("total").toString());
        this.updated = Long.parseLong(taskStatus.get("updated").toString());
        this.created = Long.parseLong(taskStatus.get("created").toString());
        this.deleted = Long.parseLong(taskStatus.get("deleted").toString());
        this.batches = Integer.parseInt(taskStatus.get("batches").toString());
        this.versionConflicts = Long.parseLong(taskStatus.get("version_conflicts").toString());
        this.noops = Long.parseLong(taskStatus.get("noops").toString());
        this.requestsPerSecond = Float.parseFloat(taskStatus.get("requests_per_second").toString());
        this.throttledUntilMillis = Long.parseLong(taskStatus.get("throttled_until_millis").toString());
        this.throttled = Long.parseLong(taskStatus.get("throttled_millis").toString());
        HashMap<String, Integer> parsedRetries = (HashMap<String, Integer>) (taskStatus.get("retries"));
        this.bulkRetries = parsedRetries.get("bulk");
        this.searchRetries = parsedRetries.get("search");
        if (total > 0) {
            this.setExecutionProgress(Math.round(((updated + created) * 100) / total));
        } else {
            this.setExecutionProgress(0);
        }
    }

    public synchronized void updateStatus(final JsonNode taskResults) {
        final ObjectMapper mapper = new ObjectMapper();
        if (taskResults.get("response") != null) {
            if (taskResults.get("response").get("failures") != null &&
                    taskResults.get("response").get("failures").size() > 0) {
                this.failures = new LinkedList<String>() {{
                    add(taskResults.get("response").get("failures").get(0).toString());
                }};
            }
            if (taskResults.get("response").get("canceled") != null) {
                this.error = taskResults.get("response").get("canceled");
            }
        }

        if (taskResults.get("error") != null) {
            this.error = taskResults.get("error");
        }

        if (total > 0) {
            this.setExecutionProgress(Math.round(((updated + created) * 100) / total));
        } else {
            this.setExecutionProgress(0);
        }

        this.setEndTime(getStartTime() + runningTimeNanos / 1000 / 1000);
    }

    public synchronized void updateStatuses() {
        if ((isDone() && (this.updated + this.created) < this.total) || error != null || failures.size() > 0) {
            this.setFailed(true);
        } else {
            this.setSucceeded(true);
        }
        this.setInActiveProcess(false);
    }

    public Map<String, Object> getReprocessParams() {
        return reprocessParams;
    }

    public void setReprocessParams(Map<String, Object> reprocessParams) {
        this.reprocessParams = reprocessParams;
    }

    public String getSelfGeneratedTaskId() {
        return selfGeneratedTaskId;
    }

    public void setSelfGeneratedTaskId(String selfGeneratedTaskId) {
        this.selfGeneratedTaskId = selfGeneratedTaskId;
    }

    public void setSourceIndex(String sourceIndex) {
        this.sourceIndex = sourceIndex;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getReindexParams() {
        return reindexParams;
    }

    public void setReindexParams(String reindexParams) {
        this.reindexParams = reindexParams;
    }

    public Object getError() {
        return error;
    }

    public void setError(Object error) {
        this.error = error;
    }

    public List<String> getFailures() {
        return failures;
    }

    public void setFailures(List<String> failures) {
        this.failures = failures;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getDescription() {
        return description;
    }

    public String getSourceIndex() {
        return sourceIndex;
    }

    public String getTaskId() {
        return taskId;
    }

    public long getRunningTimeNanos() {
        return runningTimeNanos;
    }

    public void setRunningTimeNanos(long runningTimeNanos) {
        this.runningTimeNanos = runningTimeNanos;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public long getUpdated() {
        return updated;
    }

    public void setUpdated(long updated) {
        this.updated = updated;
    }

    public long getCreated() {
        return created;
    }

    public void setCreated(long created) {
        this.created = created;
    }

    public long getDeleted() {
        return deleted;
    }

    public void setDeleted(long deleted) {
        this.deleted = deleted;
    }

    public int getBatches() {
        return batches;
    }

    public void setBatches(int batches) {
        this.batches = batches;
    }

    public long getVersionConflicts() {
        return versionConflicts;
    }

    public void setVersionConflicts(long versionConflicts) {
        this.versionConflicts = versionConflicts;
    }

    public long getNoops() {
        return noops;
    }

    public void setNoops(long noops) {
        this.noops = noops;
    }

    public long getBulkRetries() {
        return bulkRetries;
    }

    public void setBulkRetries(long bulkRetries) {
        this.bulkRetries = bulkRetries;
    }

    public long getSearchRetries() {
        return searchRetries;
    }

    public void setSearchRetries(long searchRetries) {
        this.searchRetries = searchRetries;
    }

    public long getThrottled() {
        return throttled;
    }

    public void setThrottled(long throttled) {
        this.throttled = throttled;
    }

    public float getRequestsPerSecond() {
        return requestsPerSecond;
    }

    public void setRequestsPerSecond(float requestsPerSecond) {
        this.requestsPerSecond = requestsPerSecond;
    }

    public long getThrottledUntilMillis() {
        return throttledUntilMillis;
    }

    public void setThrottledUntilMillis(long throttledUntilMillis) {
        this.throttledUntilMillis = throttledUntilMillis;
    }


}
