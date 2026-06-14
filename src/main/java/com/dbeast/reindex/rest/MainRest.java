package com.dbeast.reindex.rest;

import com.dbeast.reindex.app_settings.AppSettingsPOJO;
import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

import static io.javalin.apibuilder.ApiBuilder.*;

public class MainRest {
    private static final Logger logger = LogManager.getLogger();
    private static final Map<String, String> corsHeaders = new HashMap<>();
    private final AppSettingsPOJO appSettings;
    private Javalin app;

    static {
        corsHeaders.put("Access-Control-Allow-Methods", "GET,PUT,POST,DELETE");
        corsHeaders.put("Access-Control-Allow-Origin", "*");
        corsHeaders.put("Access-Control-Allow-Headers", "Content-Type,Authorization,X-Requested-With,Content-Length,Accept,Origin,Content-Disposition," +
                "Accept-Encoding,Accept-Language,Sec-Fetch-Dest,Sec-Fetch-Site,Sec-Fetch-Mode,Connection,Referer,User-Agent,Host");
        corsHeaders.put("Access-Control-Allow-Credentials", "true");
    }

    public MainRest(final AppSettingsPOJO appSettings) {
        this.appSettings = appSettings;
    }

    public void runServer(final String host, final int port) throws Exception {
        try {
            logger.info("Server Host: " + host + " Port: " + port);

            app = Javalin.create(config -> {
                config.staticFiles.add(appSettings.getInternals().getClientFolder(), Location.EXTERNAL);
                config.http.defaultContentType = "application/json";
                config.routing.treatMultipleSlashesAsSingleSlash = true;
            }).exception(Exception.class, (e, ctx) -> {
                logger.error("Error while running REST server! Exception: " + e);
                if (e.getCause() != null && e.getCause().getMessage() != null &&
                    e.getCause().getMessage().contains("Address already in use")) {
                    System.exit(-1);
                }
                ctx.status(500);
                ctx.result("Internal Server Error");
            }).error(404, ctx -> {
                logger.warn("Got incorrect URI request from ip: " + ctx.ip() +
                        " Requested URI: " + ctx.path() +
                        " Request body: " + ctx.body());
                ctx.result("Page: " + ctx.path() + " not found");
            });

            initServerSettings();
            initRestAPIs();

            app.start(host, port);
        } catch (Exception e) {
            throw new Exception("The problem in server running! Exception: " + e);
        }
    }

    /**
     * Initialize all REST APIs
     */
    private void initRestAPIs() {
        RESTReindexSettings connectionSettingsPage = new RESTReindexSettings();
        RESTReindexMonitoring reindexMonitorPage = new RESTReindexMonitoring();
        RESTProjectsMonitoring projectsMonitoring = new RESTProjectsMonitoring();

        app.routes(() -> {
            path("/reindexer", () -> {
                connectionSettingsPage.rest();
                reindexMonitorPage.rest();
                projectsMonitoring.rest();

                get("/get_url", ctx -> {
                    logger.info("Got request for application URL");
                    ctx.json(Map.of("serverBaseUrl", "http://" + appSettings.getApp().getHost() + ":" + appSettings.getApp().getPort()));
                });
            });
        });
    }

    /**
     * Initialize REST server settings
     */
    private void initServerSettings() {
        // CORS headers
        app.before(ctx -> {
            corsHeaders.forEach(ctx::header);
            ctx.contentType("application/json");
        });

        // CORS options
        app.options("/*", ctx -> {
            String accessControlRequestHeaders = ctx.header("Access-Control-Request-Headers");
            if (accessControlRequestHeaders != null) {
                ctx.header("Access-Control-Allow-Headers", accessControlRequestHeaders);
            }
            String accessControlRequestMethod = ctx.header("Access-Control-Request-Method");
            if (accessControlRequestMethod != null) {
                ctx.header("Access-Control-Allow-Methods", accessControlRequestMethod);
            }
            ctx.result("OK");
        });
    }

}

