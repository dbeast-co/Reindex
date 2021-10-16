package com.dbeast.reindex.data_warehouse;

import com.dbeast.reindex.reindex_execution_plan_monitoring.ProjectStatusPOJO;
import com.dbeast.reindex.reindex_execution_plan_monitoring_for_ui.projects_monitoring.ProjectStatusForUIProjectsMonitoringPOJO;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ProjectsMonitoringController {

    DataWarehouse dataWarehouse = DataWarehouse.getInstance();
    private final Map<String, ProjectStatusPOJO> projectsStatus = dataWarehouse.getProjectsStatus();

    public List<ProjectStatusForUIProjectsMonitoringPOJO> getProjectsStatusForUI() {
        return projectsStatus.values().stream()
                .map(ProjectStatusForUIProjectsMonitoringPOJO::new)
                .collect(Collectors.toList());
    }
}
