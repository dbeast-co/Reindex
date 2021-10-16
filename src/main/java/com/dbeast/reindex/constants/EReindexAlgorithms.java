package com.dbeast.reindex.constants;

import java.util.Arrays;

public enum EReindexAlgorithms {
    TIME_ORIENTED("Time oriented"),
    WHOLE_INDEX("Whole index");

    private final String varForUI;

    EReindexAlgorithms(final String varForUI){
        this.varForUI = varForUI;
    }

    public String getVarForUI() {
        return varForUI;
    }

    public static EReindexAlgorithms getByValue(String value){
        return Arrays.stream(EReindexAlgorithms.values()).filter(enumValue -> enumValue.varForUI.equals(value)).findFirst().orElse(TIME_ORIENTED);
    }

}
