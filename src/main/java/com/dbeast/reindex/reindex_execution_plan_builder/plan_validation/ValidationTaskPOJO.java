package com.dbeast.reindex.reindex_execution_plan_builder.plan_validation;

import com.dbeast.reindex.reindex_execution_plan_builder.dao.IClusterTaskDAO;

public class ValidationTaskPOJO {

    private final IClusterTaskDAO requestDAO;
    private boolean isSucceeded;
    private String validationParam;

    public ValidationTaskPOJO(IClusterTaskDAO requestDAO, String validationParam) {
        this.requestDAO = requestDAO;
        this.validationParam = validationParam;
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
}
