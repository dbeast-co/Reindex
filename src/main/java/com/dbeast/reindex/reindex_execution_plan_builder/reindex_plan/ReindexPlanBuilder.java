package com.dbeast.reindex.reindex_execution_plan_builder.reindex_plan;

import com.dbeast.reindex.exceptions.ClusterConnectionException;
import com.dbeast.reindex.project_settings.IndexFromListPOJO;
import com.dbeast.reindex.project_settings.ProjectPOJO;
import com.dbeast.reindex.elasticsearch.dao.IClusterTaskDAO;
import com.dbeast.reindex.elasticsearch.dao.index.CreateIndexDAO;
import com.dbeast.reindex.reindex_execution_plan_builder.reindex_algorithms.IReindexAlgorithm;
import com.dbeast.reindex.reindex_execution_plan_builder.reindex_algorithms.ReindexAlgorithmPOJO;
import com.dbeast.reindex.reindex_execution_plan_monitoring.ProjectStatusPOJO;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.elasticsearch.index.reindex.ReindexRequest;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ReindexPlanBuilder {
    private ProjectPOJO project;
    private boolean isInActiveProcess;
    private ReindexPlanPOJO reindexPlan;

    public ReindexPlanBuilder(final ProjectPOJO project) {
        this.project = project;
        this.reindexPlan = new ReindexPlanPOJO(project);
    }

    public ReindexPlanBuilder() {
    }

    public ReindexPlanPOJO generatePlan() {
        reindexPlan.setReindexJobs(generateReindexJobs());
        reindexPlan.setClusterTasks(generateClusterTasks());
        return reindexPlan;
    }

    public ReindexPlanPOJO generatePlanFromFailure() {
        reindexPlan.setReindexJobs(generateReindexJobs());
        reindexPlan.setClusterTasks(new LinkedList<>());
        return reindexPlan;
    }


    public ReindexPlanPOJO generatePlanFromReprocessing(final ProjectStatusPOJO projectStatus) {
        reindexPlan.setReindexJobs(generateReindexJobs(projectStatus));
        return reindexPlan;
    }

    //TODO need to get parameters from monitoring (status etc)
    private Map<String, ReindexJobPOJO> generateReindexJobs(final ProjectStatusPOJO projectStatus) {
        Map<String, ReindexJobPOJO> reindexJobs = new HashMap<>();
        List<IReindexAlgorithm> reindexAlgorithms = project.getReindexSettings().getReindexAlgorithms().stream()
                .filter(ReindexAlgorithmPOJO::isSelected)
                .collect(Collectors.toList());
        IReindexAlgorithm reindexAlgorithm = reindexAlgorithms.get(0);
        projectStatus.getReindexJobsStatus().forEach(job -> {
            ReindexJobPOJO reindexJob = new ReindexJobPOJO(job.getSourceIndex());
            List<ReindexTaskPOJO> reindexRequests =
                    job.getReindexTasks().stream()
                            .map(task -> reindexAlgorithm.generateRequestFromReprocessing(
                                    project.getConnectionSettings().getSource(),
                                    task.getSourceIndex(),
                                    project.getReindexSettings().booleanReindexType(),
                                    task.getReprocessParams()
                            ))
                            .collect(Collectors.toList());
            reindexRequests.forEach(request -> {
                request.setReindexRequest(generateDestination(request.getReindexRequest(), job.getSourceIndex()));
                reindexJob.addReindexTask(request);
            });
            reindexJobs.put(job.getSourceIndex(), reindexJob);
        });
        return reindexJobs;
    }

    private Map<String, ReindexJobPOJO> generateReindexJobs() {
        Map<String, ReindexJobPOJO> reindexJobs = new HashMap<>();
        List<IReindexAlgorithm> reindexAlgorithms = project.getReindexSettings().getReindexAlgorithms().stream()
                .filter(ReindexAlgorithmPOJO::isSelected)
                .collect(Collectors.toList());
        if (reindexAlgorithms.size() > 0) {
            IReindexAlgorithm reindexAlgorithm = reindexAlgorithms.get(0);
            List<String> indicesForReindex = project.getIndexList().stream()
                    .filter(IndexFromListPOJO::isIs_checked)
                    .map(IndexFromListPOJO::getIndexName)
                    .collect(Collectors.toList());
            indicesForReindex.forEach(index -> {
                ReindexJobPOJO reindexJob = new ReindexJobPOJO(index);
                List<ReindexTaskPOJO> reindexTasksList = null;
                try {
                    reindexTasksList = reindexAlgorithm.generateRequests(
                            project.getConnectionSettings().getSource(),
                            index,
                            project.getReindexSettings().booleanReindexType(),
                            project.getProjectId()
                    );
                } catch (ClusterConnectionException e) {
                    e.printStackTrace();
                }
                reindexTasksList.forEach(request -> {
                    request.setReindexRequest(generateDestination(request.getReindexRequest(), index));
                    reindexJob.addReindexTask(request);
                });
                reindexJobs.put(index, reindexJob);
            });
        }
        return reindexJobs;
    }

    private List<ClusterTaskPOJO> generateClusterTasks() {
        //Create first index for ILM
        List<ClusterTaskPOJO> clusterTasks = new LinkedList<>();
        if (project.getReindexSettings().isUseIlm() && project.getReindexSettings().isCreateFirstIndexOnRollover()) {
            IClusterTaskDAO createIndexDAO = new CreateIndexDAO(
                    "create index with alias",
                    project.getReindexSettings().getSendToRolloverAliasAlias(),
                    project.getReindexSettings().getCreateFirstIndexOnRolloverIndexName());
            clusterTasks.add(new ClusterTaskPOJO(createIndexDAO));
        }

        //TODO Transfer index settings and templates
        return clusterTasks;
    }

    private ReindexRequest generateDestination(final ReindexRequest request,
                                               final String sourceIndex) {
        String destinationIndex;
        if (project.getReindexSettings().isMergeToOneIndex()) {
            destinationIndex = project.getReindexSettings().getMergeToOneIndexIndexName();
        } else if (project.getReindexSettings().isSendToAlias()) {
            destinationIndex = project.getReindexSettings().getSendToAliasAlias();
        } else if (project.getReindexSettings().isUseIlm() && project.getReindexSettings().isSendToRolloverAlias()) {
            destinationIndex = project.getReindexSettings().getSendToRolloverAliasAlias();
        } else if (project.getReindexSettings().isAddIndexPrefix() && project.getReindexSettings().isAddIndexSuffix()) {
            destinationIndex = project.getReindexSettings().getAddIndexPrefixPrefix() + sourceIndex + project.getReindexSettings().getAddIndexSuffixSuffix();
        } else if (project.getReindexSettings().isAddIndexPrefix()) {
            destinationIndex = project.getReindexSettings().getAddIndexPrefixPrefix() + sourceIndex;
        } else if (project.getReindexSettings().isAddIndexSuffix()) {
            destinationIndex = sourceIndex + project.getReindexSettings().getAddIndexSuffixSuffix();
        } else if (project.getReindexSettings().isRemoveIndexSuffix()) {
            destinationIndex = sourceIndex.substring(0, sourceIndex.length() - project.getReindexSettings().getRemoveIndexSuffixSuffix().length());
        } else if (project.getReindexSettings().isUseSameIndexName()) {
            destinationIndex = sourceIndex;
        } else {
            destinationIndex = sourceIndex;
        }
        if (project.getReindexSettings().isSendToPipeline()) {
            request.setDestPipeline(project.getReindexSettings().getIsSendToPipelinePipelineName());
        }
        request.setDestIndex(destinationIndex);
        return request;
    }

    @JsonProperty("is_in_active_process")
    public boolean isInActiveProcess() {
        return isInActiveProcess;
    }

    public void setInActiveProcess(boolean inActiveProcess) {
        isInActiveProcess = inActiveProcess;
    }

    public ProjectPOJO getProject() {
        return project;
    }

    public void setProject(ProjectPOJO project) {
        this.project = project;
    }

}
