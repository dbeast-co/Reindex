package com.dbeast.reindex.data_warehouse;

import com.dbeast.reindex.project_settings.EsSettings;
import com.dbeast.reindex.project_settings.IndexFromListPOJO;
import com.dbeast.reindex.project_settings.ProjectPOJO;
import com.dbeast.reindex.app_settings.AppSettingsPOJO;
import com.dbeast.reindex.constants.EAppSettings;
import com.dbeast.reindex.constants.EClusterStatus;
import com.dbeast.reindex.constants.EProjectStatus;
import com.dbeast.reindex.constants.EReindexType;
import com.dbeast.reindex.elasticsearch.ElasticsearchController;
import com.dbeast.reindex.exceptions.ClusterConnectionException;
import com.dbeast.reindex.project_execution_plan_runner.ReindexRunner;
import com.dbeast.reindex.project_settings.TemplateFromListPOJO;
import com.dbeast.reindex.reindex_execution_plan_builder.plan_validation.ValidationPlanBuilder;
import com.dbeast.reindex.reindex_execution_plan_builder.plan_validation.ValidationPlanExecutor;
import com.dbeast.reindex.reindex_execution_plan_builder.plan_validation.ValidationPlanPOJO;
import com.dbeast.reindex.reindex_execution_plan_builder.plan_validation.ValidationResponsePOJO;
import com.dbeast.reindex.reindex_execution_plan_builder.reindex_plan.ReindexTaskPOJO;
import com.dbeast.reindex.reindex_execution_plan_builder.reindex_plan.ReindexPlanBuilder;
import com.dbeast.reindex.reindex_execution_plan_builder.reindex_plan.ReindexPlanPOJO;
import com.dbeast.reindex.reindex_execution_plan_monitoring.ProjectStatusPOJO;
import com.dbeast.reindex.reindex_execution_plan_monitoring.ReindexStatusBuilder;
import com.dbeast.reindex.reindex_execution_plan_monitoring.ReindexTaskStatusPOJO;
import com.dbeast.reindex.reindex_execution_plan_monitoring_for_ui.project_settings_page.ProjectStatusForUIProjectSettingsPagePOJO;
import com.dbeast.reindex.utils.GeneralUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import spark.Request;

import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletException;
import javax.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ReindexSettingsController {
    private static final Logger logger = LogManager.getLogger();

    DataWarehouse dataWarehouse = DataWarehouse.getInstance();

    private final Map<String, ProjectPOJO> projectsMap = dataWarehouse.getProjectsMap();
    private final Map<String, ReindexPlanPOJO> projectsReindexPlanMap = dataWarehouse.getProjectsReindexPlanMap();
    private final Map<String, ProjectStatusPOJO> projectsStatus = dataWarehouse.getProjectsStatus();
    private final Map<String, ReindexRunner> projectsExecutors = dataWarehouse.getProjectsExecutors();

    private final AppSettingsPOJO appSettings = dataWarehouse.getAppSettings();
    private final ElasticsearchController elasticsearchController = new ElasticsearchController();


    private static final ReindexSettingsController _instance = new ReindexSettingsController();

    public static ReindexSettingsController getInstance() {
        if (_instance == null) {
            return new ReindexSettingsController();
        }
        return _instance;
    }

    private ReindexSettingsController() {
    }

    public List<Map<String, String>> getProjectsList() {
        List<Map<String, String>> projectList = new LinkedList<>();
        projectsMap.forEach((id, project) -> {
            Map<String, String> projectSettings = new HashMap<>();
            projectSettings.put("project_id", project.getProjectId());
            projectSettings.put("project_name", project.getProjectName());
            projectList.add(projectSettings);
        });
        return projectList;
    }

    public boolean retryFailures(final String projectId) {
        ReindexPlanPOJO reindexPlan;
        if (!projectsReindexPlanMap.containsKey(projectId)) {
            reindexPlan = generateReindexPlan(projectsMap.get(projectId));
        } else {
            reindexPlan = projectsReindexPlanMap.get(projectId);
        }
        ProjectStatusPOJO projectStatus = projectsStatus.get(projectId);
        reindexPlan.setFailed(false);
        reindexPlan.setDone(false);
        reindexPlan.setStatus(EProjectStatus.ON_FLY);
        reindexPlan.getReindexJobs().values()
                .forEach(job -> job.getReindexTasks().stream()
                        .filter(ReindexTaskPOJO::isFailed)
                        .forEach(task -> {
                            task.setFailed(false);
                            task.setDone(false);
                            job.setDone(false);
                        }));
        projectStatus.setFailed(false);
        projectStatus.setDone(false);
        projectStatus.setFailedTasks(0);
        projectStatus.setStatus(EProjectStatus.ON_FLY);
        projectStatus.setExecutionProgress((projectStatus.getSucceededTasks() * 100) / projectStatus.getTasksNumber());
        projectStatus.setStartTime(0);
        projectStatus.setEndTime(0);
        projectStatus.getReindexJobsStatus()
                .forEach(job -> {
                    job.setFailed(false);
                    job.getReindexTasks().stream()
                            .filter(ReindexTaskStatusPOJO::isFailed)
                            .forEach(task -> {
                                task.setFailed(false);
                                task.setDone(false);
                                task.setFailures(new LinkedList<>());
                                task.setError(null);
                                task.setStatus(EProjectStatus.NEW);
                                task.setStartTime(0);
                                task.setEndTime(0);
                                task.setExecutionProgress(0);
                                job.setDone(false);
                                job.setFailedTasks(0);
                                job.setStatus(EProjectStatus.NEW);
                                job.setStartTime(0);
                                job.setEndTime(0);
                                job.setExecutionProgress((job.getSucceededTasks() * 100) / job.getTasksNumber());
                            });
                });
        ReindexRunner reindexRunner = new ReindexRunner(appSettings.getInternals().getTasksRefreshInterval(), appSettings.getApp().getTasksAPIRetriesNumber());
        projectsExecutors.put(projectId, reindexRunner);
        return reindexRunner.executePlan(reindexPlan);
    }


    public boolean runProject(final String projectId) {
        ReindexPlanPOJO reindexPlan;
//        if (!projectsReindexPlanMap.containsKey(projectId)) {
//            reindexPlan = generateReindexPlan(projectsMap.get(projectId));
        reindexPlan = projectsReindexPlanMap.get(projectId);
        projectsReindexPlanMap.put(projectId, reindexPlan);
//        } else {
//        }
        generateReindexStatus(reindexPlan);
        ReindexRunner reindexRunner = new ReindexRunner(appSettings.getInternals().getTasksRefreshInterval(), appSettings.getApp().getTasksAPIRetriesNumber());
        projectsExecutors.put(projectId, reindexRunner);
        return reindexRunner.executePlan(reindexPlan);
    }

    public void stopProject(final String projectId) {
        ReindexRunner projectsExecutor = projectsExecutors.get(projectId);
        if (projectsStatus.get(projectId).isInActiveProcess()) {
            projectsExecutor.stop();
        }
    }

    public ValidationResponsePOJO prepareProject(final String projectId) throws ClusterConnectionException {
        ReindexPlanPOJO reindexPlan = generateReindexPlan(projectsMap.get(projectId));
        projectsReindexPlanMap.put(projectId, reindexPlan);
        generateReindexStatus(reindexPlan);
        ValidationPlanBuilder validationPlanBuilder = new ValidationPlanBuilder(reindexPlan, projectsMap.get(projectId));
        ValidationPlanPOJO validationPlan = validationPlanBuilder.generateValidationPlan();
        ValidationPlanExecutor validationPlanExecutor = new ValidationPlanExecutor(validationPlan);
        if (logger.isDebugEnabled()) {
            logger.debug(validationPlan.toString());
        }
        return validationPlanExecutor.executePlan();
    }

    public ProjectStatusForUIProjectSettingsPagePOJO getProjectStatusForSettingsPageForId(final String projectId) {
        if (projectsStatus.containsKey(projectId)) {
            return projectsStatus.get(projectId).generateProjectStatusForUIProjectSettingsPage();
        } else {
            return new ProjectStatusForUIProjectSettingsPagePOJO(0, EProjectStatus.NEW, false, false, false);
        }
    }

    public boolean deleteProjectById(final String projectId) {
        boolean isDeleted = true;
        if (projectsMap.containsKey(projectId)) {
            isDeleted = GeneralUtils.deleteFile(appSettings.getInternals().getProjectsFolder() + projectId);
            projectsMap.remove(projectId);
            projectsStatus.remove(projectId);
            projectsExecutors.remove(projectId);
            projectsReindexPlanMap.remove(projectId);
        }
        return isDeleted;
    }

    public void getSources(final ProjectPOJO project) throws ClusterConnectionException {
        List<HashMap<String, String>> indices = elasticsearchController.getIndexList(project.getConnectionSettings().getSource(), project.getProjectId());
        List<IndexFromListPOJO> indexList = indices.stream()
                .map(IndexFromListPOJO::new)
                .collect(Collectors.toList());
        List<HashMap<String, String>> templates = elasticsearchController.getTemplateList(project.getConnectionSettings().getSource(), project.getProjectId());
        List<TemplateFromListPOJO> templateList = templates.stream()
                .map(TemplateFromListPOJO::new)
                .collect(Collectors.toList());

        project.setIndexList(indexList);
        project.setTemplateList(templateList);
    }

    public boolean validateIsProjectNameExists(final String projectName) {
        List<String> projectNames = projectsMap.values().stream()
                .map(ProjectPOJO::getProjectName)
                .collect(Collectors.toList());
        return projectNames.contains(projectName);
    }

    public boolean saveProject(final ProjectPOJO project) {
        if (project.getReindexSettings().getReindexType().equals(EReindexType.LOCAL.getReindexType())) {
            project.getConnectionSettings().setDestination(project.getConnectionSettings().getSource().clone());
        }
        projectsMap.put(project.getProjectId(), project);
        boolean res;
        res = GeneralUtils.createFolder(appSettings.getInternals().getProjectsFolder() + project.getProjectId());
        projectsStatus.put(project.getProjectId(), new ProjectStatusPOJO(project.getProjectId(), project.getProjectName()));
        res = GeneralUtils.createFile(appSettings.getInternals().getProjectsFolder() +
                        project.getProjectId() + "/" + EAppSettings.PROJECT_MONITORING_FILE.getStringValueOfSetting(),
                projectsStatus.get(project.getProjectId())) && res;
//        }
        res = GeneralUtils.createFile(appSettings.getInternals().getProjectsFolder() +
                        project.getProjectId() + "/" + EAppSettings.PROJECT_SETTINGS_FILE.getStringValueOfSetting(),
                projectsMap.get(project.getProjectId())) && res;
        return res;
    }

    public ProjectPOJO getProjectById(final String projectId) {
        if (projectsMap.containsKey(projectId)) {
            projectsMap.get(projectId).setProjectStatus(projectsStatus.get(projectId).generateProjectStatusForUIProjectSettingsPage());
            return projectsMap.get(projectId);
        } else {
            ProjectPOJO project = dataWarehouse.getNewProjectTemplate();
            project.setProjectId(projectId);
            return project;
        }
    }

    public ProjectPOJO getNewProject() {
        return dataWarehouse.getNewProjectTemplate();
    }

    //TODO  update delete old cert
    public boolean uploadSSLCert(final Request request) {
        String location = appSettings.getInternals().getProjectsFolder() +
                request.params(":projectId") + "/";
        GeneralUtils.createFolder(location);
        request.attribute("org.eclipse.jetty.multipartConfig", new MultipartConfigElement(location));
        try {
            // "file" is the key of the form data with the file itself being the value
            Part filePart = request.raw().getPart("file");

            // The name of the file user uploaded
            String uploadedFileName = filePart.getSubmittedFileName();

            InputStream stream = filePart.getInputStream();
            // Write stream to file under storage folder
            Files.copy(stream, Paths.get(location + request.params(":usage") + "_" +
                    uploadedFileName), StandardCopyOption.REPLACE_EXISTING);
//            projectsMap.get(request.params(":projectId")).getConnectionSettings()
            return true;

        } catch (ServletException | IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public String getClusterStatus(final EsSettings connectionSettings,
                                   final String projectId) {
        try {
            return "{\"cluster_status\" : \"" + elasticsearchController.getClusterStatus(connectionSettings, projectId) + "\"}";
        } catch (Exception e) {
            return "{\"status\":{\"cluster_status\" : \"" +
                    EClusterStatus.ERROR +
                    "\"},\"error\":\"There is an error while command execution: " +
                    e.getMessage() +
                    "\"}";
        }
    }

    public String getIndexParameters(final EsSettings connectionSettings,
                                     final String index,
                                     final String projectId) throws ClusterConnectionException {
        return elasticsearchController.getIndexParameters(connectionSettings, index, projectId);
    }

    public String getTemplateParameters(final EsSettings connectionSettings,
                                        final String template,
                                        final String projectId) throws ClusterConnectionException {
        return elasticsearchController.getTemplateParameters(connectionSettings, template, projectId);
    }

    private ReindexPlanPOJO generateReindexPlan(final ProjectPOJO project) {
        ReindexPlanBuilder reindexPlanBuilder = new ReindexPlanBuilder(project);
        ReindexPlanPOJO reindexPlan = reindexPlanBuilder.generatePlan();
        projectsReindexPlanMap.put(project.getProjectId(), reindexPlan);
        logger.debug("Generated plan: " + reindexPlan.toString());
        return reindexPlan;
    }

    private void generateReindexStatus(final ReindexPlanPOJO reindexPlan) {
        ReindexStatusBuilder reindexStatusBuilder = new ReindexStatusBuilder(reindexPlan);
        ProjectStatusPOJO projectStatus = reindexStatusBuilder.generateProjectStatus();
        projectsStatus.put(reindexPlan.getProjectId(), projectStatus);

    }

}