package com.dbeast.reindex.project_settings;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Objects;


public class ConnectionSettingsPOJO implements Cloneable {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private EsSettings source = new EsSettings();
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private EsSettings destination = new EsSettings();

    public String sourceOrDestinationExists() {
        if (source != null) {
            return "source";
        } else {
            return "destination";
        }
    }

    @Override
    public ConnectionSettingsPOJO clone() {
        ConnectionSettingsPOJO result;
        try {
            result = (ConnectionSettingsPOJO) super.clone();
            result.source = source.clone();
            result.destination = destination.clone();
            return result;
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            return this;
        }
    }

    public EsSettings getSource() {
        return source;
    }

    public void setSource(EsSettings source) {
        this.source = source;
    }

    public EsSettings getDestination() {
        return destination;
    }

    public void setDestination(EsSettings destination) {
        this.destination = destination;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ConnectionSettingsPOJO)) return false;
        ConnectionSettingsPOJO that = (ConnectionSettingsPOJO) o;
        return Objects.equals(getSource(), that.getSource()) &&
                Objects.equals(getDestination(), that.getDestination());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getSource(), getDestination());
    }

    @Override
    public String toString() {
        return "ConnectionSettingsPOJO{" +
                "source=" + source.toString() +
                ", destination=" + destination.toString() +
                '}';
    }
}
