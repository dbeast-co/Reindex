package com.dbeast.reindex.reindex_execution_plan_builder.reindex_plan;

import com.dbeast.reindex.reindex_execution_plan_builder.dao.IClusterTaskDAO;
import com.dbeast.reindex.reindex_execution_plan_monitoring.BasicStatusPOJO;

public class ClusterTaskPOJO extends BasicStatusPOJO {

    private String selfGeneratedTaskId;
    private IClusterTaskDAO requestDAO;
    private String description;

    public ClusterTaskPOJO(final IClusterTaskDAO requestDAO) {
        this.requestDAO = requestDAO;
        this.description = requestDAO.getDescription();
        this.selfGeneratedTaskId = requestDAO.getSelfGeneratedTaskId();
    }

    public IClusterTaskDAO getRequestDAO() {
        return requestDAO;
    }

    public void setRequestDAO(IClusterTaskDAO requestDAO) {
        this.requestDAO = requestDAO;
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
}
