package com.dbeast.reindex.reindex_execution_plan_builder.plan_validation;

import com.dbeast.reindex.constants.EValidationStatuses;
import com.dbeast.reindex.constants.EValidationTasksSettings;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.LinkedList;
import java.util.List;

public class ValidationResponsePOJO {

    @JsonProperty("project_id")
    private String projectId;
    @JsonProperty("project_name")
    private String projectName;
    @JsonProperty("status")
    private EValidationStatuses status = EValidationStatuses.NEW;
    @JsonProperty("validation_results")
    private List<ValidationResult> validationResults = new LinkedList<>();
    @JsonProperty("user_message")
    private String userMessage;

    public ValidationResponsePOJO(String projectId, String projectName) {
        this.projectId = projectId;
        this.projectName = projectName;
    }

    public int calculateProjectStatus() {
        int numericStatus = validationResults.stream()
                .map(ValidationResult::getNumericStatus)
                .max(Integer::compareTo)
                .orElse(0);
        this.status = EValidationStatuses.getByIntValue(numericStatus);
        if (numericStatus > 0) {
            this.userMessage = status.getUserMessage();
        }
        return numericStatus;
    }

    public void addValidationResponse(ValidationResult validationResult) {
        validationResults.add(validationResult);
    }

    static class ValidationResult {
        @JsonProperty("validation_param")
        private String validationParam;
        @JsonProperty("task")
        private String task;
        @JsonProperty("status")
        private String status;
        @JsonProperty("notes")
        private String notes;

        public ValidationResult(final String validationParam, final EValidationTasksSettings task) {
            this.task = String.format(task.getTaskExplanation(), validationParam);
            this.status = task.getOnFailureStatus().getValidationStatus();
            this.notes = task.getOnFailureNotes();
            this.validationParam = validationParam;
        }

        public ValidationResult(final EValidationTasksSettings task) {
            this.task = task.getTaskExplanation();
            this.status = task.getOnFailureStatus().getValidationStatus();
            this.notes = task.getOnFailureNotes();
            this.validationParam = "";
        }

        public void updateStatus(final boolean isSucceeded) {
            if (isSucceeded) {
                setStatus(EValidationStatuses.PASS);
                setEmptyNotes();
            }
        }

        private void setEmptyNotes() {
            this.notes = "";
        }


        public String getTask() {
            return task;
        }

        public void setTask(EValidationTasksSettings task) {
            this.task = task.getTaskExplanation();
        }

        public EValidationStatuses getStatus() {
            return EValidationStatuses.valueOf(status);
        }

        public int getNumericStatus() {
            return EValidationStatuses.valueOf(status).getNumericStatus();
        }

        public void setStatus(EValidationStatuses status) {
            this.status = status.getValidationStatus();
        }

        public String getNotes() {
            return notes;
        }

        public void setNotes(EValidationTasksSettings notes) {
            this.notes = notes.getOnFailureNotes();
        }

        public String getValidationParam() {
            return validationParam;
        }

        public void setValidationParam(String validationParam) {
            this.validationParam = validationParam;
        }
    }

    private void updateUserMessage() {

    }

    public String getUserMessage() {
        return userMessage;
    }

    public void setUserMessage(String userMessage) {
        this.userMessage = userMessage;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public EValidationStatuses getStatus() {
        return status;
    }

    public void setStatus(EValidationStatuses status) {
        this.status = status;
    }

    public List<ValidationResult> getValidationResults() {
        return validationResults;
    }

    public void setValidationResults(List<ValidationResult> validationResults) {
        this.validationResults = validationResults;
    }
}
