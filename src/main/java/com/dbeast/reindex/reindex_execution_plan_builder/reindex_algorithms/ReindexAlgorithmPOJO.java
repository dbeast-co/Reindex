package com.dbeast.reindex.reindex_execution_plan_builder.reindex_algorithms;

import com.dbeast.reindex.exceptions.ClusterConnectionException;
import com.dbeast.reindex.project_settings.EsSettings;
import com.dbeast.reindex.reindex_execution_plan_builder.reindex_plan.ReindexTaskPOJO;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ReindexAlgorithmPOJO implements IReindexAlgorithm {
    @JsonProperty("reindex_algorithm_name")
    protected String reindexAlgorithmName;
    @JsonProperty("algorithm_params")
    protected List<AlgorithmParam> algorithmParams = new LinkedList<>();
    @JsonProperty("is_selected")
    protected boolean isSelected;

    public ReindexAlgorithmPOJO(final ReindexAlgorithmPOJO algorithmSettingsFromClient) {
        this.reindexAlgorithmName = algorithmSettingsFromClient.reindexAlgorithmName;
        this.algorithmParams = algorithmSettingsFromClient.algorithmParams;
        this.isSelected = algorithmSettingsFromClient.isSelected;
    }

    public ReindexAlgorithmPOJO(final String reindexAlgorithmName,
                                final List<AlgorithmParam> algorithmParams,
                                final boolean isSelected) {
        this.reindexAlgorithmName = reindexAlgorithmName;
        this.algorithmParams = algorithmParams;
        this.isSelected = isSelected;
    }

    public ReindexAlgorithmPOJO(final String reindexAlgorithmName,
                                final List<AlgorithmParam> algorithmParams) {
        this.reindexAlgorithmName = reindexAlgorithmName;
        this.algorithmParams = algorithmParams;
    }

    public ReindexAlgorithmPOJO() {
    }

    @Override
    public List<ReindexTaskPOJO> generateRequests(final EsSettings source,
                                                  final String index,
                                                  final boolean isRemote,
                                                  final String projectId) throws ClusterConnectionException {
        return null;
    }

    ;

    @Override
    public ReindexTaskPOJO generateRequestFromReprocessing(final EsSettings source,
                                                           final String index,
                                                           final boolean isRemote,
                                                           final Map<String, Object> reprocessingParams) {
        return null;
    }

    @Override
    @JsonProperty("is_selected")
    public boolean isSelected() {
        return isSelected;
    }

    public static class AlgorithmParam {
        private String label;
        @JsonProperty("field_type")
        private String fieldType;
        @JsonProperty("default_value")
        private Object defaultValue;
        @JsonProperty("actual_value")
        private Object actualValue;

        public AlgorithmParam(final String label,
                              final String fieldType,
                              final Object defaultValue,
                              final Object actualValue) {
            this.label = label;
            this.fieldType = fieldType;
            this.defaultValue = defaultValue;
            this.actualValue = actualValue;
        }

        public AlgorithmParam() {
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public String getFieldType() {
            return fieldType;
        }

        public void setFieldType(String fieldType) {
            this.fieldType = fieldType;
        }

        public Object getDefaultValue() {
            return defaultValue;
        }

        public void setDefaultValue(Object defaultValue) {
            this.defaultValue = defaultValue;
        }

        public Object getActualValue() {
            return actualValue;
        }

        public void setActualValue(Object actualValue) {
            this.actualValue = actualValue;
        }

        @Override
        public String toString() {
            return "AlgorithmParam{" +
                    "label='" + label + '\'' +
                    ", fieldType='" + fieldType + '\'' +
                    ", defaultValue=" + defaultValue +
                    ", actualValue=" + actualValue +
                    '}';
        }
    }

    public String getReindexAlgorithmName() {
        return reindexAlgorithmName;
    }


    public void setReindexAlgorithmName(String reindexAlgorithmName) {
        this.reindexAlgorithmName = reindexAlgorithmName;
    }

    public List<AlgorithmParam> getAlgorithmParams() {
        return algorithmParams;
    }

    public void setAlgorithmParams(List<AlgorithmParam> algorithmParams) {
        this.algorithmParams = algorithmParams;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}
