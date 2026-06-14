package com.dbeast.reindex.rest;

import com.dbeast.reindex.data_warehouse.ReindexMonitoringController;
import com.dbeast.reindex.reindex_execution_plan_monitoring_for_ui.monitoring_page.ProjectMonitoringForProjectMonitoringUIPOJO;

import static io.javalin.apibuilder.ApiBuilder.*;

public class RESTReindexMonitoring extends ARest {
    private final ReindexMonitoringController reindexMonitoringController = new ReindexMonitoringController();

    @Override
    public void rest() {
        path("/reindex_monitoring", () -> {
            get("/get/{projectId}", ctx -> {
                String projectId = ctx.pathParam("projectId");
                if (logger.isDebugEnabled()) {
                    logger.info("Got request for project monitoring with id: " + projectId);
                }
                ProjectMonitoringForProjectMonitoringUIPOJO res = reindexMonitoringController.getReindexMonitoringByProjectId(projectId);
                if (res != null) {
                    ctx.json(res);
                } else {
                    ctx.status(204);
                    ctx.result("There is no project monitoring data yet. Please save the project and try again.");
                }
            });

            get("/list", ctx -> {
                logger.info("Got request for projects monitoring");
                ctx.json(reindexMonitoringController.getProjectsStatusForClient());
            });

            get("/get_tasks_report/{projectId}", ctx -> {
                String projectId = ctx.pathParam("projectId");
                logger.info("Got request for task report. Project and tasks id: " + projectId);
                ctx.json(reindexMonitoringController.getTaskReportById(projectId));
            });
        });
    }
}
