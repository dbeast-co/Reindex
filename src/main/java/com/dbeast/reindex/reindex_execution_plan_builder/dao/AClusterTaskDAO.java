package com.dbeast.reindex.reindex_execution_plan_builder.dao;

import com.dbeast.reindex.utils.GeneralUtils;
import com.dbeast.reindex.reindex_execution_plan_monitoring.ClusterTaskStatusPOJO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.ElasticsearchStatusException;
import org.elasticsearch.client.RestHighLevelClient;

public abstract class AClusterTaskDAO<T> {
    protected static final Logger logger = LogManager.getLogger();

    protected String selfGeneratedTaskId = GeneralUtils.generateNewUID();
    protected final String requestType;
    protected final String[] params;
    protected String description;

    public AClusterTaskDAO(String requestType, String[] params) {
        this.requestType = requestType;
        this.params = params;
    }

    public boolean execute(final RestHighLevelClient client, final ClusterTaskStatusPOJO status) {
        boolean result = false;
        status.setInActiveProcess(true);
        status.setStartTime(System.currentTimeMillis());
        try {
            result = executeRequest(getRequest(), client);
            status.setExecutionProgress(100);
        } catch (ElasticsearchStatusException e) {
            logger.error(e.getDetailedMessage());
            status.setException(e.getDetailedMessage());
            status.setExecutionProgress(0);
        } finally {
            status.setEndTime(System.currentTimeMillis());
            status.setSucceeded(result);
            status.setFailed(!result);
            status.setInActiveProcess(false);
            status.setDone(true);
        }
        return result;
    }

    protected abstract T generateRequest();

    protected abstract boolean executeRequest(T request, RestHighLevelClient client);

    protected abstract T getRequest();

    public String getSelfGeneratedTaskId() {
        return selfGeneratedTaskId;
    }

    public void setSelfGeneratedTaskId(String selfGeneratedTaskId) {
        this.selfGeneratedTaskId = selfGeneratedTaskId;
    }

    public String getRequestType() {
        return requestType;
    }

    public String[] getParams() {
        return params;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
