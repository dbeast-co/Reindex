package com.dbeast.reindex.constants;

public enum EClusterStatus {
    GREEN("Test success! Cluster work properly"),
    YELLOW("Test success, but may have a problem"),
    RED("Test failed! The cluster unreachable or not respond "),
    ERROR("Test failed! Connection refused "),
    UNTESTED("");

    private final String statusDescription;

    EClusterStatus(final String statusDescription){
        this.statusDescription = statusDescription;
    }

    public String getStatusDescription() {
        return statusDescription;
    }
}
