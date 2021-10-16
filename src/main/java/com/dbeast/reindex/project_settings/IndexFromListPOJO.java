package com.dbeast.reindex.project_settings;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;

public class IndexFromListPOJO {
    @JsonProperty("index_name")
    private String indexName = "";

    @JsonProperty("is_checked")
    private boolean is_checked;

    public IndexFromListPOJO(HashMap<String, String> index) {
        this.indexName = index.get("index");
        this.is_checked = false;
    }

    public IndexFromListPOJO() {
    }

    public String getIndexName() {
        return indexName;
    }

    public void setIndexName(String indexName) {
        this.indexName = indexName;
    }

    public boolean isIs_checked() {
        return is_checked;
    }

    public void setIs_checked(boolean is_checked) {
        this.is_checked = is_checked;
    }

    @Override
    public String toString() {
        return "IndexFromListPOJO{" +
                "indexName='" + indexName + '\'' +
                ", is_checked=" + is_checked +
                '}';
    }
}
