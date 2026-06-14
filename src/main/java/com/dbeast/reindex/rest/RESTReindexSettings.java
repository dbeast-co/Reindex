package com.dbeast.reindex.rest;

import com.dbeast.reindex.data_warehouse.ReindexSettingsController;
import com.dbeast.reindex.project_settings.EsSettingsPOJO;
import com.dbeast.reindex.project_settings.ProjectPOJO;
import com.dbeast.reindex.reindex_execution_plan_builder.plan_validation.ValidationResponsePOJO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static io.javalin.apibuilder.ApiBuilder.*;

public class RESTReindexSettings extends ARest {
    private static final Logger logger = LogManager.getLogger();

    private final ReindexSettingsController reindexSettingsController = ReindexSettingsController.getInstance();

    @Override
    public void rest() {
        path("/reindex_settings", () -> {
            get("/new", ctx -> {
                if (logger.isDebugEnabled()) {
                    logger.debug("Got request for new project!");
                }
                ctx.json(reindexSettingsController.getNewProject());
            });

            get("/get/{id}", ctx -> {
                String id = ctx.pathParam("id");
                if (logger.isDebugEnabled()) {
                    logger.debug("Got request for project with id: " + id);
                }
                ProjectPOJO result = reindexSettingsController.getProjectById(id);
                ctx.json(result);
            });

            get("/start/{id}", ctx -> {
                String id = ctx.pathParam("id");
                logger.info("Got request for run project with id: " + id);
                ctx.json(reindexSettingsController.runProject(id));
            });

            get("/stop/{id}", ctx -> {
                String id = ctx.pathParam("id");
                logger.info("Got request for stop project with id: " + id);
                reindexSettingsController.stopProject(id);
                ctx.json(true);
            });

            get("/list", ctx -> {
                if (logger.isDebugEnabled()) {
                    logger.debug("Got requests for project list");
                }
                ctx.json(reindexSettingsController.getProjectsList());
            });

            get("/validate/{projectName}", ctx -> {
                String projectName = ctx.pathParam("projectName");
                if (logger.isDebugEnabled()) {
                    logger.debug("Got request for validate project name: " + projectName);
                }
                ctx.json(reindexSettingsController.validateIsProjectNameExists(projectName));
            });

            get("/validate/", ctx -> {
                if (logger.isDebugEnabled()) {
                    logger.debug("Got request for validate project name: empty");
                }
                ctx.json(false);
            });

            get("/get_status/{projectId}", ctx -> {
                String projectId = ctx.pathParam("projectId");
                if (logger.isDebugEnabled()) {
                    logger.debug("Got request for project monitoring with id: " + projectId);
                }
                ctx.json(reindexSettingsController.getProjectStatusForSettingsPageForId(projectId));
            });

            get("/prepare_project/{projectId}", ctx -> {
                String projectId = ctx.pathParam("projectId");
                if (logger.isDebugEnabled()) {
                    logger.debug("Got request for prepare project with id: " + projectId);
                }
                ValidationResponsePOJO res = reindexSettingsController.prepareProject(projectId);
                ctx.json(res);
            });

            get("/retry_failures/{projectId}", ctx -> {
                String projectId = ctx.pathParam("projectId");
                logger.info("Got request for retry failures of the project with id: " + projectId);
                ctx.json(reindexSettingsController.retryFailures(projectId));
            });

            post("/get_index_parameters/{projectId}/{index}", ctx -> {
                String projectId = ctx.pathParam("projectId");
                String index = ctx.pathParam("index");
                if (logger.isDebugEnabled()) {
                    logger.debug("Got request for the index parameters of the index: " + index
                            + " for project: " + projectId);
                }
                ctx.result(reindexSettingsController.getIndexParameters(
                    mapper.readValue(ctx.body(), EsSettingsPOJO.class),
                    index,
                    projectId));
            });

            post("/get_template_parameters/{projectId}/{template}", ctx -> {
                String projectId = ctx.pathParam("projectId");
                String template = ctx.pathParam("template");
                if (logger.isDebugEnabled()) {
                    logger.debug("Got request for the index parameters of the template: " + template
                            + " for project: " + projectId);
                }
                ctx.result(reindexSettingsController.getTemplateParameters(
                    mapper.readValue(ctx.body(), EsSettingsPOJO.class),
                    template,
                    projectId));
            });

            post("/get_sources", ctx -> {
                if (logger.isDebugEnabled()) {
                    logger.debug("Got request for get sources from Elasticsearch");
                    logger.debug("Request body: " + ctx.body());
                }
                ProjectPOJO project = mapper.readValue(ctx.body(), ProjectPOJO.class);
                reindexSettingsController.getSources(project);
                ctx.json(project);
            });

            post("/test_cluster/{id}", ctx -> {
                String id = ctx.pathParam("id");
                if (logger.isDebugEnabled()) {
                    logger.debug("Got request for test Elasticsearch server with id: " + id);
                    logger.debug("Request body: " + ctx.body());
                }
                EsSettingsPOJO connectionSettings = mapper.readValue(ctx.body(), EsSettingsPOJO.class);
                String responseBody = reindexSettingsController.getClusterStatus(connectionSettings, id);
                if (responseBody.contains("error")) {
                    ctx.status(502);
                }
                ctx.result(responseBody);
            });

            post("/save", ctx -> {
                if (logger.isDebugEnabled()) {
                    logger.debug("Got request for save project");
                    logger.debug("Request body: " + ctx.body());
                }
                ProjectPOJO project = mapper.readValue(ctx.body(), ProjectPOJO.class);
                ctx.json(reindexSettingsController.saveProject(project));
            });

            post("/ssl_cert/{usage}/{projectId}", ctx -> {
                if (logger.isDebugEnabled()) {
                    logger.debug("Got request for upload SSL certificate");
                }
                ctx.json(reindexSettingsController.uploadSSLCert(ctx));
            });

            delete("/delete/{id}", ctx -> {
                String id = ctx.pathParam("id");
                logger.info("Got request for delete project with Id: " + id);
                ctx.json(reindexSettingsController.deleteProjectById(id));
            });
        });
    }
}
