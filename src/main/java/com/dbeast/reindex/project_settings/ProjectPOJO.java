package com.dbeast.reindex.project_settings;

import com.dbeast.reindex.reindex_execution_plan_monitoring_for_ui.project_settings_page.ProjectStatusForUIProjectSettingsPagePOJO;
import com.dbeast.reindex.utils.GeneralUtils;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class ProjectPOJO implements Cloneable{
    @JsonProperty("project_name")
    private String projectName = "";
    @JsonProperty("project_id")
    private String projectId = GeneralUtils.generateNewUID();
    @JsonProperty("connection_settings")
    private ConnectionSettingsPOJO connectionSettings = new ConnectionSettingsPOJO();
    @JsonProperty("source_index_list")
    private List<IndexFromListPOJO> indexList = new LinkedList<>();
    @JsonProperty("source_template_list")
    private List<TemplateFromListPOJO> templateList = new LinkedList<>();
    @JsonProperty("reindex_settings")
    private ReindexSettingsPOJO reindexSettings = new ReindexSettingsPOJO();
    @JsonProperty("status")
    private ProjectStatusForUIProjectSettingsPagePOJO projectStatus = new ProjectStatusForUIProjectSettingsPagePOJO();

    public ProjectPOJO(final String project_id) {
        this.projectId = project_id;
    }

    public ProjectPOJO (final ProjectPOJO project){
        this.projectName = project.getProjectName();
        this.projectId = project.getProjectId();
        this.connectionSettings = project.getConnectionSettings();
        this.indexList = project.getIndexList();
        this.templateList = project.getTemplateList();
        this.reindexSettings = project.getReindexSettings();
        this.projectStatus = project.getProjectStatus();

    }

    public ProjectPOJO() {
    }

    @Override
    public ProjectPOJO clone()  {
        try {
        ProjectPOJO result = (ProjectPOJO) super.clone();
        result.connectionSettings = connectionSettings.clone();
        return result;
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            return this;
        }
    }

    public ProjectStatusForUIProjectSettingsPagePOJO getProjectStatus() {
        return projectStatus;
    }

    public void setProjectStatus(ProjectStatusForUIProjectSettingsPagePOJO projectStatus) {
        this.projectStatus = projectStatus;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public ConnectionSettingsPOJO getConnectionSettings() {
        return connectionSettings;
    }

    public void setConnectionSettings(ConnectionSettingsPOJO connectionSettings) {
        this.connectionSettings = connectionSettings;
    }

    public List<IndexFromListPOJO> getIndexList() {
        return indexList;
    }

    public void setIndexList(List<IndexFromListPOJO> indexList) {
        this.indexList = indexList;
    }

    public List<TemplateFromListPOJO> getTemplateList() {
        return templateList;
    }

    public void setTemplateList(List<TemplateFromListPOJO> templateList) {
        this.templateList = templateList;
    }

    public ReindexSettingsPOJO getReindexSettings() {
        return reindexSettings;
    }

    public void setReindexSettings(ReindexSettingsPOJO reindexSettings) {
        this.reindexSettings = reindexSettings;
    }


    @Override
    public String toString() {
        return "ProjectPOJO{" +
                "projectName='" + projectName + '\'' +
                ", projectId='" + projectId + '\'' +
                ", connectionSettings=" + connectionSettings.toString() +
                ", indexList=" + indexList.toString() +
                ", templateList=" + templateList.toString() +
                ", reindexSettings=" + reindexSettings.toString() +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProjectPOJO)) return false;
        ProjectPOJO that = (ProjectPOJO) o;
        return Objects.equals(getProjectName(), that.getProjectName()) &&
                Objects.equals(getConnectionSettings(), that.getConnectionSettings()) &&
                Objects.equals(getIndexList(), that.getIndexList()) &&
                Objects.equals(getTemplateList(), that.getTemplateList()) &&
                Objects.equals(getReindexSettings(), that.getReindexSettings());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getProjectName(), getConnectionSettings(), getIndexList(), getTemplateList(), getReindexSettings());
    }
}
