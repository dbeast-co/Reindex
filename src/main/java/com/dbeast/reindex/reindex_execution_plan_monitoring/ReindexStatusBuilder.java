package com.dbeast.reindex.reindex_execution_plan_monitoring;

import com.dbeast.reindex.reindex_execution_plan_builder.reindex_plan.ReindexPlanPOJO;

import java.util.stream.Collectors;

public class ReindexStatusBuilder {
    private final ReindexPlanPOJO reindexPlan;

    public ReindexStatusBuilder(final ReindexPlanPOJO reindexPlan) {
        this.reindexPlan = reindexPlan;
    }

    public ProjectStatusPOJO generateProjectStatus() {
        ProjectStatusPOJO projectStatus = new ProjectStatusPOJO(reindexPlan.getProjectId(), reindexPlan.getProjectName());
        reindexPlan.getReindexJobs().forEach((index, jobsPlan) -> {
            ReindexJobStatusPOJO jobStatus = new ReindexJobStatusPOJO(index);
            jobsPlan.getReindexTasks().forEach(taskPlan ->
                    jobStatus.getReindexTasks().add(new ReindexTaskStatusPOJO("",
                            index, taskPlan.getDescription(), taskPlan.getQuery(),
                            taskPlan.getReindexParams(), taskPlan.getReprocessParams())
                    )
            );
            jobStatus.setTasksNumber(jobStatus.getReindexTasks().size());
            projectStatus.getReindexJobsStatus().add(jobStatus);
        });
        projectStatus.setClusterTasksStatus(reindexPlan.getClusterTasks().stream()
                .map(ClusterTaskStatusPOJO::new)
                .collect(Collectors.toList()));
        projectStatus.setTasksNumber(
                projectStatus.getReindexJobsStatus()
                        .stream()
                        .mapToInt(AdvancedStatusPOJO::getTasksNumber)
                        .sum()
        );
        projectStatus.setTasksNumber(projectStatus.getTasksNumber() + projectStatus.getClusterTasksStatus().size());
        return projectStatus;
    }
}
