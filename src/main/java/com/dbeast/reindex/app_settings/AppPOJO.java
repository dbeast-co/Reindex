package com.dbeast.reindex.app_settings;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

public class AppPOJO {
    private String host;
    private int port;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("tasks_api_retries_number")
    private int tasksAPIRetriesNumber = 3;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("http_proxy")
    private String http_proxy;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("https_proxy")
    private String https_proxy;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("no_proxy")
    private String no_proxy;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("proxy_user")
    private String proxy_user;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("proxy_password")
    private String proxy_password;

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

    public String getHttp_proxy() {
        return http_proxy;
    }

    public String getHttps_proxy() {
        return https_proxy;
    }

    public String getNo_proxy() {
        return no_proxy;
    }

    public String getProxy_user() {
        return proxy_user;
    }

    public String getProxy_password() {
        return proxy_password;
    }
}
