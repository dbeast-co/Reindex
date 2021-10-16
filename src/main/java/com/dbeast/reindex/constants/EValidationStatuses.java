package com.dbeast.reindex.constants;

import java.util.Arrays;

public enum EValidationStatuses {
    NEW("","", -1),
    PASS("PASS","", 0),
    WARNING("WARNING", "There a number of potential errors in the project.\n" +
            "Are you sure, that you want to continue?", 1),
    ERROR("ERROR", "There is a number of errors in the project.\n" +
            "Please fix the errors and try again", 2);

    private final String userMessage;
    private final String validationStatus;
    private final int numericStatus;

    EValidationStatuses(final String validationStatus, final String userMessage, final int numericStatus) {
        this.userMessage = userMessage;
        this.validationStatus = validationStatus;
        this.numericStatus = numericStatus;
    }

    public String getUserMessage() {
        return this.userMessage;
    }

    public String getValidationStatus() {
        return validationStatus;
    }

    public int getNumericStatus() {
        return numericStatus;
    }

    public static EValidationStatuses getByIntValue(int value){
        return Arrays.stream(EValidationStatuses.values()).filter(enumValue -> enumValue.numericStatus == value).findFirst().orElse(NEW);
    }
}
