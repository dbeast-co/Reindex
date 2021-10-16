package com.dbeast.reindex;

import com.dbeast.reindex.app_settings.AppPOJO;
import com.dbeast.reindex.app_settings.AppSettingsPOJO;
import com.dbeast.reindex.constants.EAppSettings;
import com.dbeast.reindex.data_warehouse.DataWarehouse;
import com.dbeast.reindex.rest.MainRest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;

public class Reindex {
    private static final Logger logger = LogManager.getLogger();

    public static void main(String[] args) throws Exception {
        logger.info("Welcome to Reindex application");
        final String homeFolder = args[0];
        if (homeFolder == null) {
            logger.error("The home folder doesn't specified ");
            System.exit(-1);
        }
        logger.info("Application home folder: " + homeFolder);
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        AppSettingsPOJO appSettings;
        try {
            AppPOJO appConfiguration = mapper.readValue(new File(homeFolder +
                    EAppSettings.CONFIG_FOLDER.getStringValueOfSetting() +
                    EAppSettings.CONFIGURATION_FILE.getStringValueOfSetting()), AppPOJO.class);
            appSettings = new AppSettingsPOJO(appConfiguration, homeFolder);
        } catch (IOException e) {
            throw new Exception("Configuration folder not present or Incorrect configuration file" + e);
        }

        DataWarehouse dataWarehouse = DataWarehouse.getInstance();
        dataWarehouse.init(appSettings);
        MainRest restApi = new MainRest(appSettings);
        restApi.runServer(appSettings.getApp().getHost(), appSettings.getApp().getPort());
    }

    public static void stop() {
        System.exit(0);
    }
}
