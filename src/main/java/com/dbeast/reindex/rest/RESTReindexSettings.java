package com.dbeast.reindex.rest;

import com.dbeast.reindex.data_warehouse.ReindexSettingsController;
import com.dbeast.reindex.project_settings.EsSettings;
import com.dbeast.reindex.project_settings.ProjectPOJO;
import com.dbeast.reindex.reindex_execution_plan_builder.plan_validation.ValidationResponsePOJO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import spark.Response;

import java.util.HashMap;

import static spark.Spark.*;

public class RESTReindexSettings extends ARest {
    private static final Logger logger = LogManager.getLogger();

    private final ReindexSettingsController reindexSettingsController = ReindexSettingsController.getInstance();

    //TODO update response for error
    public void rest() {
        path("/reindex_settings", () -> {
            get("/new", (request, response) -> {
                logger.info("Got request for new project!");
                return objectToString(reindexSettingsController.getNewProject());
            });
            get("/get/:id", (request, response) -> {
                logger.info("Got request for project with id: " + request.params(":id"));
                ProjectPOJO result = reindexSettingsController.getProjectById(request.params(":id"));
                return objectToString(result);
            });
            get("/start/:id", (request, response) -> {
                logger.info("Got request for run project with id: " + request.params(":id"));
                return reindexSettingsController.runProject(request.params(":id"));
            });
            get("/stop/:id", (request, response) -> {
                logger.info("Got request for stop project with id: " + request.params(":id"));
                reindexSettingsController.stopProject(request.params(":id"));
                return true;
            });
            get("/list", (request, response) -> {
                logger.info("Got requests for project list");
                return objectToString(reindexSettingsController.getProjectsList());
            });
            get("/validate/:projectName", (request, response) -> {
                logger.info("Got request for validate project name: " + request.params("projectName"));
                return objectToString(reindexSettingsController.validateIsProjectNameExists(request.params("projectName")));
            });
            get("/validate/", (request, response) -> {
                logger.info("Got request for validate project name: " + request.params("projectName"));
                return false;
            });
            get("/get_status/:projectId", (request, response) -> {
                logger.info("Got request for project monitoring with id: " + request.params(":projectId"));
                return objectToString(reindexSettingsController.getProjectStatusForSettingsPageForId(request.params(":projectId")));
            });
            get("/prepare_project/:projectId", (request, response) -> {
                logger.info("Got request for prepare project with id: " + request.params(":projectId"));
                ValidationResponsePOJO res = reindexSettingsController.prepareProject(request.params(":projectId"));
                return objectToString(res);
            });
            get("/retry_failures/:projectId", (request, response) -> {
                logger.info("Got request for retry failures of the project with id: " + request.params(":projectId"));
                return objectToString(reindexSettingsController.retryFailures(request.params(":projectId")));
            });

            post("/get_index_parameters/:projectId/:index", (request, response) -> {
                logger.info("Got request for the index parameters of the index: " + request.params(":index")
                        + " for project: " + request.params("projectId"));
                return reindexSettingsController.getIndexParameters(mapper.readValue(request.body(), EsSettings.class),
                        request.params(":index"),
                        request.params(":projectId"));
            });
            post("/get_template_parameters/:projectId/:template", (request, response) -> {
                logger.info("Got request for the index parameters of the template: " + request.params(":template")
                        + " for project: " + request.params("projectId"));
                return reindexSettingsController.getTemplateParameters(mapper.readValue(request.body(), EsSettings.class),
                        request.params(":template"),
                        request.params(":projectId"));
            });
            post("/get_sources", (request, response) -> {
                logger.info("Got request for get sources from Elasticsearch");
                if (logger.isDebugEnabled()) {
                    logger.debug("Request body: " + request.body());
                }
                ProjectPOJO project = mapper.readValue(request.body(), ProjectPOJO.class);
                reindexSettingsController.getSources(project);
                return objectToString(project);
            });
            post("/test_cluster/:id", (request, response) -> {
                logger.info("Got request for test Elasticsearch server with id: " + request.params(":id"));
                if (logger.isDebugEnabled()) {
                    logger.debug("Request body: " + request.body());
                }
                EsSettings connectionSettings = mapper.readValue(request.body(), EsSettings.class);
                String responseBody = reindexSettingsController.getClusterStatus(connectionSettings, request.params(":id"));
                if (responseBody.contains("error")) {
                    response.status(502);
                }
                return responseBody;
            });
            post("/save", (request, response) -> {
                logger.info("Got request for save project");
                if (logger.isDebugEnabled()) {
                    logger.debug("Request body: " + request.body());
                }
                ProjectPOJO project = mapper.readValue(request.body(), ProjectPOJO.class);
                return reindexSettingsController.saveProject(project);
            });

            post("/ssl_cert/:usage/:projectId", (request, response) -> {
                logger.info("Got request for upload SSL certificate");
//                setCorsHeaders(response);
                return reindexSettingsController.uploadSSLCert(request);
            });

            delete("/delete/:id", (request, response) -> {
                logger.info("Got request for delete project with Id: " + request.params(":id"));
                return reindexSettingsController.deleteProjectById(request.params(":id"));
            });
        });
    }

    private void setCorsHeaders(Response response) {
        HashMap<String, String> corsHeaders = new HashMap<>();
        corsHeaders.put("Access-Control-Allow-Methods", "GET,PUT,POST,DELETE");
        corsHeaders.put("Access-Control-Allow-Origin", "*");
        corsHeaders.put("Access-Control-Allow-Headers", "Content-Type,Authorization,X-Requested-With,Content-Length,Accept,Origin,Content-Disposition," +
                "Accept-Encoding,Accept-Language,Sec-Fetch-Dest,Sec-Fetch-Site,Sec-Fetch-Mode,Connection,Referer,User-Agent,Host" +
                "origin, content-type, cache-control, accept, options, authorization, x-requested-with");
        corsHeaders.put("Access-Control-Allow-Credentials", "true");
        corsHeaders.put("Access-Control-Expose-Headers", "Content-Disposition");
        corsHeaders.forEach(response::header);
    }
}
