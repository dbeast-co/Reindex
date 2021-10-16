package com.dbeast.reindex.constants;

import java.util.Arrays;

public enum EReindexType {
    LOCAL("Local reindex", false),
    REMOTE("Remote reindex", true);
    private final String reindexType;
    private final boolean isRemoteReindex;

    EReindexType(final String reindexType, boolean isRemoteReindex) {
        this.reindexType = reindexType;
        this.isRemoteReindex = isRemoteReindex;
    }

    public String getReindexType() {
        return reindexType;
    }

    public static EReindexType getByValue(String value) {
        return Arrays.stream(EReindexType.values()).filter(enumValue -> enumValue.reindexType.equals(value)).findFirst().orElse(LOCAL);
    }

    public boolean isRemoteReindex() {
        return isRemoteReindex;
    }
}
