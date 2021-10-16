package com.dbeast.reindex.project_settings;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;

public class TemplateFromListPOJO {
    @JsonProperty("template_name")
    private String templateName = "";

    @JsonProperty("is_checked")
    private boolean is_checked;

    public TemplateFromListPOJO(HashMap<String, String> index) {
        this.templateName = index.get("name");
        this.is_checked = false;
    }

    public TemplateFromListPOJO() {
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public boolean isIs_checked() {
        return is_checked;
    }

    public void setIs_checked(boolean is_checked) {
        this.is_checked = is_checked;
    }

    @Override
    public String toString() {
        return "TemplateFromListPOJO{" +
                "templateName='" + templateName + '\'' +
                ", is_checked=" + is_checked +
                '}';
    }
}
