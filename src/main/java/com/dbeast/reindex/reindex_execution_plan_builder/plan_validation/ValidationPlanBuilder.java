package com.dbeast.reindex.reindex_execution_plan_builder.plan_validation;

import com.dbeast.reindex.constants.EReindexAlgorithms;
import com.dbeast.reindex.constants.EValidationTasksSettings;
import com.dbeast.reindex.elasticsearch.dao.data_stream.IsDataStreamExistsDAO;
import com.dbeast.reindex.project_settings.ProjectPOJO;
import com.dbeast.reindex.elasticsearch.dao.alias.IsAliasExistsDAO;
import com.dbeast.reindex.elasticsearch.dao.cluster_health.IsClusterRespond;
import com.dbeast.reindex.elasticsearch.dao.cluster_settings.IsReindexRemoteClusterDefined;
import com.dbeast.reindex.elasticsearch.dao.index.IsDateFieldExistsAndHaveDateTypeDAO;
import com.dbeast.reindex.elasticsearch.dao.index.IsIndexExistsDAO;
import com.dbeast.reindex.elasticsearch.dao.ingest_pipeline.IsPipelineExistsDAO;
import com.dbeast.reindex.reindex_execution_plan_builder.reindex_algorithms.ReindexAlgorithmPOJO;
import com.dbeast.reindex.project_settings.ReindexSettingsPOJO;
import com.dbeast.reindex.reindex_execution_plan_builder.reindex_plan.ReindexPlanPOJO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.stream.Collectors;

public class ValidationPlanBuilder {
    private static final Logger logger = LogManager.getLogger();
    private final ReindexPlanPOJO reindexPlan;
    private final ValidationResponsePOJO validationResponse;
    private final ProjectPOJO project;
    private final ValidationPlanPOJO validationPlan;

    public ValidationPlanBuilder(final ReindexPlanPOJO reindexPlan, final ProjectPOJO project) {
        this.reindexPlan = reindexPlan;
        this.validationResponse = new ValidationResponsePOJO(reindexPlan.getProjectId(), reindexPlan.getProjectName());
        this.project = project;
        this.validationPlan = new ValidationPlanPOJO(project);
    }

    public ValidationPlanPOJO generateValidationPlan() {
        ReindexSettingsPOJO reindexSettings = project.getReindexSettings();

        List<? extends ReindexAlgorithmPOJO> currentAlgorithm = project.getReindexSettings().getReindexAlgorithms().stream()
                .filter(ReindexAlgorithmPOJO::isSelected)
                .collect(Collectors.toList());
        //Check is at least one destination selected
        if (!reindexSettings.checkIsAtLeastOneDestinationSelected()) {
            validationResponse.addValidationResponse(new ValidationResponsePOJO.ValidationResult(EValidationTasksSettings.IS_DESTINATION_SELECTED));
        } else {
            //Check is at least one of the indices selected
            if (reindexPlan.getReindexJobs().isEmpty()) {
                validationResponse.addValidationResponse(new ValidationResponsePOJO.ValidationResult(EValidationTasksSettings.IS_INDEX_SELECTED));
            } else {
                //Check is cluster respond
                generateValidationForIsClusterRespond();

                //Validate the tasks number less than 1000
                int totalTasksNumber = reindexPlan.getReindexJobs().values().stream()
                        .mapToInt(reindexJobPOJO -> reindexJobPOJO.getReindexTasks().size())
                        .sum();
                if (totalTasksNumber > 1000) {
                    validationResponse.addValidationResponse(new ValidationResponsePOJO.ValidationResult(Integer.toString(totalTasksNumber), EValidationTasksSettings.IS_TO_MANY_TASKS));
                }

                //Check is the date field have the type date
                if (!currentAlgorithm.isEmpty() && currentAlgorithm.get(0).getReindexAlgorithmName().equals(EReindexAlgorithms.TIME_ORIENTED.getVarForUI())) {
                    String dateField = currentAlgorithm.get(0).getAlgorithmParams().stream()
                            .filter(algorithmParam -> algorithmParam.getLabel().equals("Date field"))
                            .map(ReindexAlgorithmPOJO.AlgorithmParam::getActualValue)
                            .findFirst().orElse("@timestamp").toString();
                    reindexPlan.getReindexJobs().values().stream()
                            .filter(job -> !job.getReindexTasks().isEmpty())
                            .forEach(job -> generateValidationForTimeSeriesAlgorithmTask(job.getIndexName(), dateField));
                }

                //Check is the ingest pipeline exists
                if (reindexSettings.isSendToPipeline()) {
                    generateValidationForIsPipelineExistsTask(project.getReindexSettings().getIsSendToPipelinePipelineName());
                }

                //Check is the expected indices exists
                if (reindexSettings.isMergeToOneIndex()) {
                    generateValidationForIsIndexExistsTask(reindexSettings.getMergeToOneIndexIndexName());
                }

                //Check is the expected indices exists
                if (reindexSettings.isAddIndexSuffix() || reindexSettings.isAddIndexPrefix()) {
                    reindexPlan.getReindexJobs().values().stream()
                            .filter(job -> !job.getReindexTasks().isEmpty())
                            .forEach(job -> generateValidationForIsIndexExistsTask(job.getReindexTasks().get(0).getReindexRequest().getDestination().index()));
                }

                //Check is alias exists
                if (reindexSettings.isSendToAlias()) {
                    generateValidationForIsAliasExistsTask(project.getReindexSettings().getSendToAliasAlias());
                }

                //Check is index contains suffix
                if (reindexSettings.isRemoveIndexSuffix()) {
                    reindexPlan.getReindexJobs().values().stream()
                            .filter(job -> !job.getReindexTasks().isEmpty())
                            .filter(job -> !job.getIndexName().endsWith(project.getReindexSettings().getRemoveIndexSuffixSuffix()))
                            .forEach(job ->
                                    validationResponse.addValidationResponse(new ValidationResponsePOJO.ValidationResult(job.getIndexName(), EValidationTasksSettings.IS_INDEX_SUFFIX_EXISTS))
                            );
                    reindexPlan.getReindexJobs().values().stream()
                            .filter(job -> !job.getReindexTasks().isEmpty())
                            .forEach(job -> generateValidationForIsIndexExistsTask(job.getReindexTasks().get(0).getReindexRequest().getDestination().index()));
                }
                if (reindexSettings.isUseIlm() && reindexSettings.isSendToRolloverAlias()) {
                    //Check is rollover alias exists
                    generateValidationForIsAliasExistsTask(project.getReindexSettings().getSendToRolloverAliasAlias());
                    if (reindexSettings.isCreateFirstIndexOnRollover()) {
                        generateValidationForIsIndexExistsTask(reindexSettings.getCreateFirstIndexOnRolloverIndexName());
                    }
                }
                if (reindexSettings.isSendToDataStream()){
                    generateValidationForIsDataStreamExistsTask(reindexSettings.getSendToDataStreamStreamName());
                }
                if (reindexSettings.getReindexType().equals("Remote reindex")) {
                    generateValidationForIsReindexRemoteClusterDefined(reindexPlan.getConnectionSettings().getSource().getEs_host());
                }
            }
        }
        validationPlan.setValidationResponse(validationResponse);
        return validationPlan;
    }

    private void generateValidationForTimeSeriesAlgorithmTask(final String index, final String dateField) {
        IsDateFieldExistsAndHaveDateTypeDAO isDateFieldExistsAndHaveDateType = new IsDateFieldExistsAndHaveDateTypeDAO(index, dateField);
        validationPlan.getValidationTasks().add(new ValidationTaskPOJO(isDateFieldExistsAndHaveDateType,
                dateField + " exists for index: " + index));
        validationResponse.addValidationResponse(new ValidationResponsePOJO.ValidationResult(dateField + " exists for index: " + index,
                EValidationTasksSettings.IS_DATE_FIELD_EXISTS));
    }

    private void generateValidationForIsReindexRemoteClusterDefined(final String host) {
        IsReindexRemoteClusterDefined isReindexRemoteClusterDefined = new IsReindexRemoteClusterDefined(host);
        validationPlan.getValidationTasks().add(new ValidationTaskPOJO(isReindexRemoteClusterDefined, host));
        validationResponse.addValidationResponse(new ValidationResponsePOJO.ValidationResult(host, EValidationTasksSettings.IS_WHITELIST_EXISTS_IN_THE_REMOTE_CLUSTER));
    }

    private void generateValidationForIsAliasExistsTask(final String alias) {
        IsAliasExistsDAO isAliasExistsDAO = new IsAliasExistsDAO(alias);
        validationPlan.getValidationTasks().add(new ValidationTaskPOJO(isAliasExistsDAO, alias));
        validationResponse.addValidationResponse(new ValidationResponsePOJO.ValidationResult(alias, EValidationTasksSettings.IS_ALIAS_EXISTS));
    }

    private void generateValidationForIsIndexExistsTask(final String index) {
        IsIndexExistsDAO isIndexExistsDAO = new IsIndexExistsDAO(index);
        validationPlan.getValidationTasks().add(new ValidationTaskPOJO(isIndexExistsDAO, index));
        validationResponse.addValidationResponse(new ValidationResponsePOJO.ValidationResult(index, EValidationTasksSettings.IS_INDEX_EXISTS));
    }

    private void generateValidationForIsPipelineExistsTask(final String pipeline) {
        IsPipelineExistsDAO isPipelineExistsDAO = new IsPipelineExistsDAO(pipeline);
        validationPlan.getValidationTasks().add(new ValidationTaskPOJO(isPipelineExistsDAO, pipeline));
        validationResponse.addValidationResponse(new ValidationResponsePOJO.ValidationResult(pipeline, EValidationTasksSettings.IS_PIPELINE_EXISTS));
    }


    private void generateValidationForIsDataStreamExistsTask(final String dataStreamName) {
        IsDataStreamExistsDAO isDataStreamExistsDAO = new IsDataStreamExistsDAO(dataStreamName);
        validationPlan.getValidationTasks().add(new ValidationTaskPOJO(isDataStreamExistsDAO, dataStreamName));
        validationResponse.addValidationResponse(new ValidationResponsePOJO.ValidationResult(dataStreamName, EValidationTasksSettings.IS_DATA_STREAM_EXISTS));
    }

    private void generateValidationForIsClusterRespond() {
        IsClusterRespond isClusterRespond = new IsClusterRespond();
        validationPlan.getValidationTasks().add(new ValidationTaskPOJO(isClusterRespond, "responding"));
        validationResponse.addValidationResponse(new ValidationResponsePOJO.ValidationResult("responding", EValidationTasksSettings.IS_CLUSTER_RESPOND));
    }

    public ValidationResponsePOJO getValidationResponse() {
        return validationResponse;
    }

    public ReindexPlanPOJO getReindexPlan() {
        return reindexPlan;
    }

    public ValidationPlanPOJO getValidationPlan() {
        return validationPlan;
    }
}
