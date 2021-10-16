package com.dbeast.reindex.constants;

public enum EAppSettings {
    PROJECT_MONITORING_FILE("project_monitoring.json"),
    PROJECT_SETTINGS_FILE("project_settings.json"),
    CONFIGURATION_FILE("reindex.yml"),
    PROJECTS_FOLDER("/projects/"),
    CONFIG_FOLDER("/config/"),
    CLIENT_FOLDER("/client/");

    private final String stringValueOfSetting;

    EAppSettings(final String statusDescription){
        this.stringValueOfSetting = statusDescription;
    }

    public String getStringValueOfSetting() {
        return stringValueOfSetting;
    }
}
