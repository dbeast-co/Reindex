package com.dbeast.reindex.app_settings;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

public class AppPOJO {
    private String host;
    private int port;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("tasks_api_retries_number")
    private int tasksAPIRetriesNumber = 3;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getTasksAPIRetriesNumber() {
        return tasksAPIRetriesNumber;
    }

    public void setTasksAPIRetriesNumber(int tasksAPIRetriesNumber) {
        this.tasksAPIRetriesNumber = tasksAPIRetriesNumber;
    }
}
