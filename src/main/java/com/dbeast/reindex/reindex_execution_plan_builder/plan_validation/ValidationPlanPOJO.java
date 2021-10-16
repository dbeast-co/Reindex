package com.dbeast.reindex.reindex_execution_plan_builder.plan_validation;

import com.dbeast.reindex.constants.EValidationStatuses;
import com.dbeast.reindex.project_settings.ConnectionSettingsPOJO;
import com.dbeast.reindex.project_settings.ProjectPOJO;
import com.dbeast.reindex.project_settings.ReindexSettingsPOJO;

import java.util.LinkedList;
import java.util.List;

public class ValidationPlanPOJO {
    private final String projectId;
    private final String projectName;
    private final ConnectionSettingsPOJO connectionSettings;
    private final ReindexSettingsPOJO reindexSettings;
    private List<ValidationTaskPOJO> validationTasks = new LinkedList<>();
    private EValidationStatuses status = EValidationStatuses.NEW;
    private ValidationResponsePOJO validationResponse;

    public ValidationPlanPOJO(ProjectPOJO project) {
        this.projectId = project.getProjectId();
        this.projectName = project.getProjectName();
        this.connectionSettings = project.getConnectionSettings();
        this.reindexSettings = project.getReindexSettings();
    }

    public ValidationResponsePOJO generateValidationResponse(){
        return new ValidationResponsePOJO(projectId, projectName);
    }

    public String getProjectId() {
        return projectId;
    }

    public String getProjectName() {
        return projectName;
    }

    public List<ValidationTaskPOJO> getValidationTasks() {
        return validationTasks;
    }

    public void setValidationTasks(List<ValidationTaskPOJO> validationTasks) {
        this.validationTasks = validationTasks;
    }

    public EValidationStatuses getStatus() {
        return status;
    }

    public void setStatus(EValidationStatuses status) {
        this.status = status;
    }

    public ConnectionSettingsPOJO getConnectionSettings() {
        return connectionSettings;
    }

    public ValidationResponsePOJO getValidationResponse() {
        return validationResponse;
    }

    public void setValidationResponse(ValidationResponsePOJO validationResponse) {
        this.validationResponse = validationResponse;
    }

    public ReindexSettingsPOJO getReindexSettings() {
        return reindexSettings;
    }
}
