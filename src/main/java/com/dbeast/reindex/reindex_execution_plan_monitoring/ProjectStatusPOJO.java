package com.dbeast.reindex.reindex_execution_plan_monitoring;

import com.dbeast.reindex.reindex_execution_plan_monitoring_for_ui.monitoring_page.ReindexTaskStatusForUIMonitoringPagePOJO;
import com.dbeast.reindex.reindex_execution_plan_monitoring_for_ui.monitoring_page.ReindexTaskStatusOnFlyForUIMonitoringPagePOJO;
import com.dbeast.reindex.reindex_execution_plan_monitoring_for_ui.project_settings_page.ProjectStatusForUIProjectSettingsPagePOJO;
import com.dbeast.reindex.reindex_execution_plan_monitoring_for_ui.projects_monitoring.ProjectStatusForUIProjectsMonitoringPOJO;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class ProjectStatusPOJO extends AdvancedStatusPOJO {
    @JsonProperty("project_id")
    private String projectId;
    @JsonProperty("project_name")
    private String projectName;
    @JsonProperty("reindex_jobs_status")
    private List<ReindexJobStatusPOJO> reindexJobsStatus = new LinkedList<>();
    @JsonProperty("cluster_tasks_status")
    private List<ClusterTaskStatusPOJO> clusterTasksStatus = new LinkedList<>();


    public ProjectStatusPOJO(String projectId, String projectName) {
        this.projectId = projectId;
        this.projectName = projectName;
    }

    public ProjectStatusPOJO() {
    }

    public synchronized void updateTransferredDocsCount() {
        setTransferredDocs(reindexJobsStatus.stream()
                .mapToLong(ReindexJobStatusPOJO::getTransferredDocs)
                .sum());
    }

    public List<ReindexTaskStatusForUIMonitoringPagePOJO> generateTaskStatusListForUIMonitoringPage() {
        return reindexJobsStatus.stream()
                .map(job -> job.getReindexTasks().stream()
                        .map(task -> {
                            if (task.isInActiveProcess()) {
                                return new ReindexTaskStatusOnFlyForUIMonitoringPagePOJO(task);
                            } else {
                                return new ReindexTaskStatusForUIMonitoringPagePOJO(task);
                            }
                        })
                        .collect(Collectors.toList())
                ).flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    public ProjectStatusForUIProjectSettingsPagePOJO generateProjectStatusForUIProjectSettingsPage() {
        return new ProjectStatusForUIProjectSettingsPagePOJO(
                getExecutionProgress(),
                getStatus(),
                isDone(),
                isFailed(),
                isInActiveProcess()
        );
    }

    public ProjectStatusForUIProjectsMonitoringPOJO generateProjectStatusForUIProjectsMonitoring() {
        return new ProjectStatusForUIProjectsMonitoringPOJO(this);
    }

    public void addReindexJobStatus(final ReindexJobStatusPOJO status) {
        reindexJobsStatus.add(status);
    }

    public void addMetadataTaskStatus(final ClusterTaskStatusPOJO status) {
        clusterTasksStatus.add(status);
    }

    public String getProjectId() {
        return projectId;
    }

    public String getProjectName() {
        return projectName;
    }

    public List<ReindexJobStatusPOJO> getReindexJobsStatus() {
        return reindexJobsStatus;
    }

    public void setReindexJobsStatus(List<ReindexJobStatusPOJO> reindexJobsStatus) {
        this.reindexJobsStatus = reindexJobsStatus;
    }

    public List<ClusterTaskStatusPOJO> getClusterTasksStatus() {
        return clusterTasksStatus;
    }

    public void setClusterTasksStatus(List<ClusterTaskStatusPOJO> clusterTasksStatus) {
        this.clusterTasksStatus = clusterTasksStatus;
    }

    public void updateStatus() {
        List<ReindexJobStatusPOJO> failedJobs = reindexJobsStatus.stream()
                .filter(ReindexJobStatusPOJO::isFailed)
                .collect(Collectors.toList());
        if (failedJobs.size() > 0) {
            setFailed(true);
        } else {
            if (!isInterrupted()) {
                setSucceeded(true);
            }
        }
        setDone(!isInterrupted());
    }

}

