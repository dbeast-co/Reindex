package com.dbeast.reindex.rest;

import com.dbeast.reindex.data_warehouse.ReindexMonitoringController;
import com.dbeast.reindex.reindex_execution_plan_monitoring_for_ui.monitoring_page.ProjectMonitoringForProjectMonitoringUIPOJO;

import static spark.Spark.get;
import static spark.Spark.path;

public class RESTReindexMonitoring extends ARest {
    private final ReindexMonitoringController reindexMonitoringController = new ReindexMonitoringController();

    @Override
    public void rest() {
        path("/reindex_monitoring", () -> {
            get("/get/:projectId", (request, response) -> {
                if (logger.isDebugEnabled()) {
                    logger.info("Got request for project monitoring with id: " + request.params(":projectId"));
                }
                ProjectMonitoringForProjectMonitoringUIPOJO res = reindexMonitoringController.getReindexMonitoringByProjectId(request.params(":projectId"));
                if (res != null) {
                    return objectToString(res);
                } else {
                    response.status(204);
                    return "There is no project monitoring data yet. Please save the project and try again.";
                }
            });
            get("/list", (request, response) -> {
                logger.info("Got request for projects monitoring");
                return objectToString(reindexMonitoringController.getProjectsStatusForClient());
            });
            get("/get_tasks_report/:projectId", (request, response) -> {
                logger.info("Got request for task report. Project and tasks id: " + request.params(":projectId"));
                return objectToString(reindexMonitoringController.getTaskReportById(request.params(":projectId")));
            });
        });
    }
}
