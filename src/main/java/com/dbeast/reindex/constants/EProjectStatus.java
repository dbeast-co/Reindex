package com.dbeast.reindex.constants;

import java.util.Arrays;

public enum EProjectStatus {
    NEW("NEW"),
    ON_FLY("ON FLY"),
    FAILED("FAILED"),
    SUCCEEDED("SUCCEEDED"),
    STOPPED("STOPPED");

    private final String statusDescription;

    EProjectStatus(final String statusDescription){
        this.statusDescription = statusDescription;
    }

    public String getStatusDescription() {
        return statusDescription;
    }

    public static EProjectStatus getByValue(String value){
        return Arrays.stream(EProjectStatus.values()).filter(enumValue -> enumValue.statusDescription.equals(value)).findFirst().orElse(NEW);
    }
}
