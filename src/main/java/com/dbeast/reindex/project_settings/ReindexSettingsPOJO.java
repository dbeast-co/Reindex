package com.dbeast.reindex.project_settings;

import com.dbeast.reindex.constants.EReindexType;
import com.dbeast.reindex.reindex_execution_plan_builder.reindex_algorithms.ReindexAlgorithmPOJO;
import com.dbeast.reindex.reindex_execution_plan_builder.reindex_algorithms.TimeSeriesReindexAlgorithm;
import com.dbeast.reindex.reindex_execution_plan_builder.reindex_algorithms.WholeIndexReindexAlgorithm;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class ReindexSettingsPOJO {

    @JsonProperty("is_merge_to_one_index")
    private boolean isMergeToOneIndex;
    @JsonProperty("merge_to_one_index_index_name")
    private String mergeToOneIndexIndexName = "";
    @JsonProperty("is_send_to_alias")
    private boolean isSendToAlias;
    @JsonProperty("send_to_alias_alias")
    private String sendToAliasAlias = "";
    @JsonProperty("is_send_to_pipeline")
    private boolean isSendToPipeline;
    @JsonProperty("send_to_pipeline_pipeline_name")
    private String isSendToPipelinePipelineName = "";
    @JsonProperty("is_add_prefix")
    private boolean isAddIndexPrefix;
    @JsonProperty("add_prefix_prefix")
    private String addIndexPrefixPrefix = "";
    @JsonProperty("is_add_suffix")
    private boolean isAddIndexSuffix;
    @JsonProperty("add_suffix_suffix")
    private String addIndexSuffixSuffix = "";
    @JsonProperty("is_remove_suffix")
    private boolean isRemoveIndexSuffix;
//    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("remove_suffix_suffix")
    private String removeIndexSuffixSuffix = "";
    @JsonProperty("is_transfer_index_settings_from_source_index")
    private boolean isTransferIndexSettingsFromSourceIndex;
    @JsonProperty("is_continue_on_failure")
    private boolean isContinueOnFailure = true;
    @JsonProperty("is_use_ilm")
    private boolean isUseIlm;
    @JsonProperty("is_send_to_rollover_alias")
    private boolean isSendToRolloverAlias;
    @JsonProperty("send_to_rollover_alias_alias")
    private String sendToRolloverAliasAlias = "";
    @JsonProperty("is_associate_with_ilm_policy")
    private boolean isAssociateWithIlmPolicy;
    @JsonProperty("associate_with_ilm_policy_policy_name")
    private String associateWithIlmPolicyPolicyName = "";
    @JsonProperty("is_create_first_index_of_rollover")
    private boolean isCreateFirstIndexOnRollover;
    @JsonProperty("create_first_index_of_rollover_index_name")
    private String createFirstIndexOnRolloverIndexName = "";
    @JsonProperty("number_of_concurrent_processed_indices")
    private int numberOfConcurrentProcessedIndices = 1;
    @JsonProperty("total_number_of_threads")
    private int threadsPerIndex = 1;
    @JsonProperty("reindex_algorithms")
    private List<? extends ReindexAlgorithmPOJO> reindexAlgorithms = new LinkedList<>();
    @JsonProperty("reindex_type")
    private String reindexType = EReindexType.LOCAL.getReindexType();
    @JsonProperty("is_use_same_index_name")
    private boolean isUseSameIndexName;

    public boolean checkIsAtLeastOneDestinationSelected() {
        return isMergeToOneIndex || isSendToAlias || isAddIndexPrefix || isAddIndexSuffix || isUseIlm || isRemoveIndexSuffix || isUseSameIndexName;
    }

    public boolean isRemoveIndexSuffix() {
        return isRemoveIndexSuffix;
    }

    public void setRemoveIndexSuffix(boolean removeIndexSuffix) {
        isRemoveIndexSuffix = removeIndexSuffix;
    }

    public String getRemoveIndexSuffixSuffix() {
        return removeIndexSuffixSuffix;
    }

    public void setRemoveIndexSuffixSuffix(String removeIndexSuffixSuffix) {
        this.removeIndexSuffixSuffix = removeIndexSuffixSuffix;
    }

    @JsonProperty("is_use_same_index_name")
    public boolean isUseSameIndexName() {
        return isUseSameIndexName;
    }

    public void setUseSameIndexName(boolean useSameIndexName) {
        isUseSameIndexName = useSameIndexName;
    }

    public String getReindexType() {
        return reindexType;
    }

    public void setReindexType(String reindexType) {
        this.reindexType = reindexType;
    }

    public boolean booleanReindexType() {
        return EReindexType.getByValue(this.reindexType).isRemoteReindex();
    }

    @JsonProperty("is_merge_to_one_index")
    public boolean isMergeToOneIndex() {
        return isMergeToOneIndex;
    }

    public void setMergeToOneIndex(boolean mergeToOneIndex) {
        isMergeToOneIndex = mergeToOneIndex;
    }

    public String getMergeToOneIndexIndexName() {
        return mergeToOneIndexIndexName;
    }

    public void setMergeToOneIndexIndexName(String mergeToOneIndexIndexName) {
        this.mergeToOneIndexIndexName = mergeToOneIndexIndexName;
    }

    @JsonProperty("is_send_to_alias")
    public boolean isSendToAlias() {
        return isSendToAlias;
    }

    public void setSendToAlias(boolean sendToAlias) {
        isSendToAlias = sendToAlias;
    }

    public String getSendToAliasAlias() {
        return sendToAliasAlias;
    }

    public void setSendToAliasAlias(String sendToAliasAlias) {
        this.sendToAliasAlias = sendToAliasAlias;
    }

    @JsonProperty("is_send_to_pipeline")
    public boolean isSendToPipeline() {
        return isSendToPipeline;
    }

    public void setSendToPipeline(boolean sendToPipeline) {
        isSendToPipeline = sendToPipeline;
    }

    public String getIsSendToPipelinePipelineName() {
        return isSendToPipelinePipelineName;
    }

    public void setIsSendToPipelinePipelineName(String isSendToPipelinePipelineName) {
        this.isSendToPipelinePipelineName = isSendToPipelinePipelineName;
    }

    @JsonProperty("is_add_prefix")
    public boolean isAddIndexPrefix() {
        return isAddIndexPrefix;
    }

    public void setAddIndexPrefix(boolean addIndexPrefix) {
        isAddIndexPrefix = addIndexPrefix;
    }

    public String getAddIndexPrefixPrefix() {
        return addIndexPrefixPrefix;
    }

    public void setAddIndexPrefixPrefix(String addIndexPrefixPrefix) {
        this.addIndexPrefixPrefix = addIndexPrefixPrefix;
    }

    @JsonProperty("is_add_suffix")
    public boolean isAddIndexSuffix() {
        return isAddIndexSuffix;
    }

    public void setAddIndexSuffix(boolean addIndexSuffix) {
        isAddIndexSuffix = addIndexSuffix;
    }

    public String getAddIndexSuffixSuffix() {
        return addIndexSuffixSuffix;
    }

    public void setAddIndexSuffixSuffix(String addIndexSuffixSuffix) {
        this.addIndexSuffixSuffix = addIndexSuffixSuffix;
    }

    @JsonProperty("is_transfer_index_settings_from_source_index")
    public boolean isTransferIndexSettingsFromSourceIndex() {
        return isTransferIndexSettingsFromSourceIndex;
    }

    public void setTransferIndexSettingsFromSourceIndex(boolean transferIndexSettingsFromSourceIndex) {
        isTransferIndexSettingsFromSourceIndex = transferIndexSettingsFromSourceIndex;
    }

    @JsonProperty("is_continue_on_failure")
    public boolean isContinueOnFailure() {
        return isContinueOnFailure;
    }

    public void setContinueOnFailure(boolean continueOnFailure) {
        isContinueOnFailure = continueOnFailure;
    }


    @JsonProperty("is_use_ilm")
    public boolean isUseIlm() {
        return isUseIlm;
    }

    public void setUseIlm(boolean useIlm) {
        isUseIlm = useIlm;
    }

    @JsonProperty("is_send_to_rollover_alias")
    public boolean isSendToRolloverAlias() {
        return isSendToRolloverAlias;
    }

    public void setSendToRolloverAlias(boolean sendToRolloverAlias) {
        isSendToRolloverAlias = sendToRolloverAlias;
    }

    public String getSendToRolloverAliasAlias() {
        return sendToRolloverAliasAlias;
    }

    public void setSendToRolloverAliasAlias(String sendToRolloverAliasAlias) {
        this.sendToRolloverAliasAlias = sendToRolloverAliasAlias;
    }

    @JsonProperty("is_associate_with_ilm_policy")
    public boolean isAssociateWithIlmPolicy() {
        return isAssociateWithIlmPolicy;
    }

    public void setAssociateWithIlmPolicy(boolean associateWithIlmPolicy) {
        isAssociateWithIlmPolicy = associateWithIlmPolicy;
    }

    public String getAssociateWithIlmPolicyPolicyName() {
        return associateWithIlmPolicyPolicyName;
    }

    public void setAssociateWithIlmPolicyPolicyName(String associateWithIlmPolicyPolicyName) {
        this.associateWithIlmPolicyPolicyName = associateWithIlmPolicyPolicyName;
    }

    @JsonProperty("is_create_first_index_of_rollover")
    public boolean isCreateFirstIndexOnRollover() {
        return isCreateFirstIndexOnRollover;
    }

    public void setCreateFirstIndexOnRollover(boolean createFirstIndexOnRollover) {
        isCreateFirstIndexOnRollover = createFirstIndexOnRollover;
    }

    public String getCreateFirstIndexOnRolloverIndexName() {
        return createFirstIndexOnRolloverIndexName;
    }

    public void setCreateFirstIndexOnRolloverIndexName(String createFirstIndexOnRolloverIndexName) {
        this.createFirstIndexOnRolloverIndexName = createFirstIndexOnRolloverIndexName;
    }

    public int getNumberOfConcurrentProcessedIndices() {
        return numberOfConcurrentProcessedIndices;
    }

    public void setNumberOfConcurrentProcessedIndices(int numberOfConcurrentProcessedIndices) {
        this.numberOfConcurrentProcessedIndices = numberOfConcurrentProcessedIndices;
    }

    public int getThreadsPerIndex() {
        return threadsPerIndex;
    }

    public void setThreadsPerIndex(int threadsPerIndex) {
        this.threadsPerIndex = threadsPerIndex;
    }


    public List<? extends ReindexAlgorithmPOJO> getReindexAlgorithms() {
        return reindexAlgorithms;
    }

    public void setReindexAlgorithms(List<ReindexAlgorithmPOJO> reindexAlgorithms) {

        this.reindexAlgorithms = reindexAlgorithms.stream()
                .map(reindexAlgorithm -> {
                    if (!(reindexAlgorithm instanceof TimeSeriesReindexAlgorithm) && !(reindexAlgorithm instanceof WholeIndexReindexAlgorithm)) {
                        switch (reindexAlgorithm.getReindexAlgorithmName()) {
                            case "Time oriented": {
                                reindexAlgorithm.getAlgorithmParams().stream()
                                        .filter(algorithmParam -> algorithmParam.getLabel().equals("Time frame"))
                                        .forEach(algorithmParam -> algorithmParam.setActualValue(
                                                Long.parseLong(String.valueOf(algorithmParam.getActualValue()))));
                                return new TimeSeriesReindexAlgorithm(reindexAlgorithm);
                            }
                            case "Whole index": {
                                return new WholeIndexReindexAlgorithm(reindexAlgorithm);
                            }
                            default: {
                                return reindexAlgorithm;
                            }
                        }
                    }
                    return reindexAlgorithm;
                })
                .collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return "ReindexSettingsPOJO{" +
                "isMergeToOneIndex=" + isMergeToOneIndex +
                ", mergeToOneIndexIndexName='" + mergeToOneIndexIndexName + '\'' +
                ", isSendToAlias=" + isSendToAlias +
                ", sendToAliasAlias='" + sendToAliasAlias + '\'' +
                ", isSendToPipeline=" + isSendToPipeline +
                ", isSendToPipelinePipelineName='" + isSendToPipelinePipelineName + '\'' +
                ", isAddIndexPrefix=" + isAddIndexPrefix +
                ", addIndexPrefixPrefix='" + addIndexPrefixPrefix + '\'' +
                ", isAddIndexSuffix=" + isAddIndexSuffix +
                ", addIndexSuffixSuffix='" + addIndexSuffixSuffix + '\'' +
                ", isTransferIndexSettingsFromSourceIndex=" + isTransferIndexSettingsFromSourceIndex +
                ", isContinueOnFailure=" + isContinueOnFailure +
                ", isUseIlm=" + isUseIlm +
                ", isSendToRolloverAlias=" + isSendToRolloverAlias +
                ", sendToRolloverAliasAlias='" + sendToRolloverAliasAlias + '\'' +
                ", isAssociateWithIlmPolicy=" + isAssociateWithIlmPolicy +
                ", associateWithIlmPolicyPolicyName='" + associateWithIlmPolicyPolicyName + '\'' +
                ", isCreateFirstIndexOnRollover=" + isCreateFirstIndexOnRollover +
                ", createFirstIndexOnRolloverIndexName='" + createFirstIndexOnRolloverIndexName + '\'' +
                ", numberOfConcurrentProcessedIndices=" + numberOfConcurrentProcessedIndices +
                ", threadsPerIndex=" + threadsPerIndex +
                ", reindexAlgorithms=" + reindexAlgorithms +
                '}';
    }
}

