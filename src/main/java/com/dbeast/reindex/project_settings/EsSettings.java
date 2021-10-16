package com.dbeast.reindex.project_settings;

import com.dbeast.reindex.constants.EClusterStatus;

import java.util.Objects;

public class EsSettings implements Cloneable {

    private String es_host = "http://localhost:9200";
    private boolean authentication_enabled = false;
    private String username;
    private String password;
    private boolean ssl_enabled = false;
    private String ssl_file;
    private EClusterStatus status = EClusterStatus.UNTESTED;

    @Override
    public EsSettings clone() {
        try {
            return (EsSettings) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            return this;
        }
    }

    public String getEs_host() {
        return es_host;
    }

    public void setEs_host(String es_host) {
        this.es_host = es_host;
    }

    public boolean isAuthentication_enabled() {
        return authentication_enabled;
    }

    public void setAuthentication_enabled(boolean authentication_enabled) {
        this.authentication_enabled = authentication_enabled;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isSsl_enabled() {
        return ssl_enabled;
    }

    public void setSsl_enabled(boolean ssl_enabled) {
        this.ssl_enabled = ssl_enabled;
    }

    public String getSsl_file() {
        return ssl_file;
    }

    public void setSsl_file(String ssl_file) {
        this.ssl_file = ssl_file;
    }

    public EClusterStatus getStatus() {
        return status;
    }

    public void setStatus(EClusterStatus status) {
        this.status = status;
    }


    public String host() {
        String[] splattedHost = es_host.split("//");
        return splattedHost[1].split(":")[0];
    }

    public String port() {
        String[] splattedHost = es_host.split("//");
        return splattedHost[1].split(":")[1];
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EsSettings)) return false;
        EsSettings that = (EsSettings) o;
        return isAuthentication_enabled() == that.isAuthentication_enabled() &&
                isSsl_enabled() == that.isSsl_enabled() &&
                Objects.equals(getEs_host(), that.getEs_host()) &&
                Objects.equals(getUsername(), that.getUsername()) &&
                Objects.equals(getPassword(), that.getPassword()) &&
                Objects.equals(getSsl_file(), that.getSsl_file()) &&
                getStatus() == that.getStatus();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getEs_host(), isAuthentication_enabled(), getUsername(), getPassword(), isSsl_enabled(), getSsl_file(), getStatus());
    }

    @Override
    public String toString() {
        return "EsSettings{" +
                "es_host='" + es_host + '\'' +
                ", authentication_enabled=" + authentication_enabled +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", ssl_enabled=" + ssl_enabled +
                ", ssl_file='" + ssl_file + '\'' +
                ", status=" + status +
                '}';
    }
}
