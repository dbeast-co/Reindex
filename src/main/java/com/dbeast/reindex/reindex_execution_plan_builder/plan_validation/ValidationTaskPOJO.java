package com.dbeast.reindex.reindex_execution_plan_builder.plan_validation;

import com.dbeast.reindex.elasticsearch.dao.IClusterTaskDAO;

public class ValidationTaskPOJO {

    private final IClusterTaskDAO requestDAO;
    private boolean isSucceeded;
    private String validationParam;
    private boolean useSourceCluster = false;

    public ValidationTaskPOJO(IClusterTaskDAO requestDAO, String validationParam) {
        this.requestDAO = requestDAO;
        this.validationParam = validationParam;
    }

    public ValidationTaskPOJO(IClusterTaskDAO requestDAO,
                              String validationParam,
                              boolean useSourceCluster) {
        this.requestDAO = requestDAO;
        this.validationParam = validationParam;
        this.useSourceCluster = useSourceCluster;
    }

    public IClusterTaskDAO getRequestDAO() {
        return requestDAO;
    }

    public boolean isSucceeded() {
        return isSucceeded;
    }

    public void setSucceeded(boolean succeeded) {
        isSucceeded = succeeded;
    }

    public String getValidationParam() {
        return validationParam;
    }

    public void setValidationParam(String validationParam) {
        this.validationParam = validationParam;
    }

    public boolean isUseSourceCluster() {
        return useSourceCluster;
    }

    public void setUseSourceCluster(boolean useSourceCluster) {
        this.useSourceCluster = useSourceCluster;
    }
}
