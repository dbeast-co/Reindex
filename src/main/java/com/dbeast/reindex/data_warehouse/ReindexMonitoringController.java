package com.dbeast.reindex.data_warehouse;

import com.dbeast.reindex.reindex_execution_plan_monitoring.ProjectStatusPOJO;
import com.dbeast.reindex.reindex_execution_plan_monitoring.ReindexTaskStatusPOJO;
import com.dbeast.reindex.reindex_execution_plan_monitoring_for_ui.monitoring_page.ProjectMonitoringForProjectMonitoringUIPOJO;
import com.dbeast.reindex.reindex_execution_plan_monitoring_for_ui.projects_monitoring.ProjectStatusForUIProjectsMonitoringPOJO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class ReindexMonitoringController {
    private static final Logger logger = LogManager.getLogger();

    DataWarehouse dataWarehouse = DataWarehouse.getInstance();

    private final Map<String, ProjectStatusPOJO> projectsStatus = dataWarehouse.getProjectsStatus();

    public List<ProjectStatusForUIProjectsMonitoringPOJO> getProjectsStatusForClient() {
        return projectsStatus.values().stream()
                .map(ProjectStatusForUIProjectsMonitoringPOJO::new)
                .collect(Collectors.toList());
    }

    public ReindexTaskStatusPOJO getTaskReportById(final String taskId) {
        String[] splatted = taskId.split(":");
        return projectsStatus.get(splatted[0]).getReindexJobsStatus().stream()
                .map(job -> job.getReindexTasks().stream()
                        .filter(task -> task.getSelfGeneratedTaskId().equals(splatted[1]))
                        .findFirst()
                        .orElse(null)
                )
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    public ProjectMonitoringForProjectMonitoringUIPOJO getReindexMonitoringByProjectId(final String projectId) {
        if (projectsStatus.containsKey(projectId)) {
            return new ProjectMonitoringForProjectMonitoringUIPOJO(projectsStatus.get(projectId));
        } else {
            logger.warn("The project with id: " + projectId + " doesn't exists");
            return new ProjectMonitoringForProjectMonitoringUIPOJO(projectId);
        }
    }
}
