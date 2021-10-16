package com.dbeast.reindex.rest;

import com.dbeast.reindex.data_warehouse.ProjectsMonitoringController;

import static spark.Spark.get;
import static spark.Spark.path;

public class RESTProjectsMonitoring extends ARest {
    private final ProjectsMonitoringController projectsMonitoringController = new ProjectsMonitoringController();

    @Override
    public void rest() {
        path("/projects_monitoring", () -> {
            get("/projects_status", (request, response) -> {
                if (logger.isDebugEnabled()) {
                    logger.info("Got request for projects monitoring");
                }
                return objectToString(projectsMonitoringController.getProjectsStatusForUI());
            });
        });
    }
}
