package com.dbeast.reindex.rest;

import com.dbeast.reindex.data_warehouse.ProjectsMonitoringController;

import static io.javalin.apibuilder.ApiBuilder.*;

public class RESTProjectsMonitoring extends ARest {
    private final ProjectsMonitoringController projectsMonitoringController = new ProjectsMonitoringController();

    @Override
    public void rest() {
        path("/projects_monitoring", () -> {
            get("/projects_status", ctx -> {
                if (logger.isDebugEnabled()) {
                    logger.info("Got request for projects monitoring");
                }
                ctx.json(projectsMonitoringController.getProjectsStatusForUI());
            });
        });
    }
}
