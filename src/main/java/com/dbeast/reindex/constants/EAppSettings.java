package com.dbeast.reindex.constants;

import static com.dbeast.reindex.Reindex.FILE_SEPARATOR;

public enum EAppSettings {

    PROJECT_MONITORING_FILE("project_monitoring.json"),
    PROJECT_SETTINGS_FILE("project_settings.json"),
    CONFIGURATION_FILE("reindex.yml"),
    PROJECTS_FOLDER(FILE_SEPARATOR +"projects" + FILE_SEPARATOR),
    CONFIG_FOLDER(FILE_SEPARATOR + "config" + FILE_SEPARATOR),
    CLIENT_FOLDER(FILE_SEPARATOR + "client" + FILE_SEPARATOR);

    private final String stringValueOfSetting;

    EAppSettings(final String statusDescription){
        this.stringValueOfSetting = statusDescription;
    }

    public String getStringValueOfSetting() {
        return stringValueOfSetting;
    }
}
