package com.dbeast.reindex.data_warehouse;

import com.dbeast.reindex.app_settings.AppSettingsPOJO;
import com.dbeast.reindex.constants.EAppSettings;
import com.dbeast.reindex.constants.EProjectStatus;
import com.dbeast.reindex.project_execution_plan_runner.ReindexRunner;
import com.dbeast.reindex.project_settings.ProjectPOJO;
import com.dbeast.reindex.reindex_execution_plan_builder.reindex_algorithms.ReindexAlgorithmPOJO;
import com.dbeast.reindex.reindex_execution_plan_builder.reindex_algorithms.TimeSeriesReindexAlgorithm;
import com.dbeast.reindex.reindex_execution_plan_builder.reindex_algorithms.WholeIndexReindexAlgorithm;
import com.dbeast.reindex.reindex_execution_plan_builder.reindex_plan.ReindexPlanBuilder;
import com.dbeast.reindex.reindex_execution_plan_builder.reindex_plan.ReindexPlanPOJO;
import com.dbeast.reindex.reindex_execution_plan_monitoring.ProjectStatusPOJO;
import com.dbeast.reindex.reindex_execution_plan_monitoring.ReindexJobStatusPOJO;
import com.dbeast.reindex.reindex_execution_plan_monitoring.ReindexTaskStatusPOJO;
import com.dbeast.reindex.utils.GeneralUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static com.dbeast.reindex.Reindex.FILE_SEPARATOR;

public class DataWarehouse {
    private static final Logger logger = LogManager.getLogger();

    private Map<String, ProjectPOJO> projectsMap = new HashMap<>();
    private Map<String, ReindexPlanPOJO> projectsReindexPlanMap = new HashMap<>();
    private Map<String, ProjectStatusPOJO> projectsStatus = new HashMap<>();
    private Map<String, ReindexRunner> projectsExecutors = new HashMap<>();

    private ProjectPOJO newProjectTemplate;
    private AppSettingsPOJO appSettings;

    private static final DataWarehouse _instance = new DataWarehouse();

    public static synchronized DataWarehouse getInstance() {
        if (_instance == null) {
            return new DataWarehouse();
        }
        return _instance;
    }

    private DataWarehouse() {
    }

    //TODO delete corrupted plan - test
    //TODO add exception
    public void init(final AppSettingsPOJO appSettings) {
        this.appSettings = appSettings;
        newProjectTemplate = new ProjectPOJO();
        List<ReindexAlgorithmPOJO> algorithms = new LinkedList<>();
        algorithms.add(new TimeSeriesReindexAlgorithm(true));
        algorithms.add(new WholeIndexReindexAlgorithm(true));
        newProjectTemplate.getReindexSettings().setReindexAlgorithms(algorithms);
        boolean isDone = GeneralUtils.createFolder(appSettings.getInternals().getProjectsFolder());
        if (!isDone) {
            System.exit(-1);
        }

        isDone = GeneralUtils.deleteFolderThatContainsSpecifiedFiles(appSettings.getInternals().getProjectsFolder(),
                EAppSettings.PROJECT_SETTINGS_FILE.getStringValueOfSetting());
        if (!isDone) {
            System.exit(-1);
        }

        List<Path> filesList = GeneralUtils.readFilesFromFolderPathOneInnerFolder(appSettings.getInternals().getProjectsFolder());

        filesList.forEach(this::readExistingProject);
        projectsStatus.entrySet().stream()
                .filter(entry -> !entry.getValue().isDone() && !entry.getValue().getStatus().equals(EProjectStatus.NEW))
                .forEach(entry -> {
                    ReindexPlanBuilder reindexPlanBuilder = new ReindexPlanBuilder(projectsMap.get(entry.getKey()));
                    ReindexPlanPOJO reindexPlan = reindexPlanBuilder.generatePlanFromReprocessing(entry.getValue());
                    projectsReindexPlanMap.put(entry.getKey(), reindexPlan);
                });
        Map<String, ReindexPlanPOJO> failedProjects = new HashMap<>();
        projectsStatus.entrySet().stream()
                .filter(entry -> entry.getValue().isFailed())
                .forEach(entry -> {
                    ReindexPlanBuilder reindexPlanBuilder = new ReindexPlanBuilder(projectsMap.get(entry.getKey()));
                    ReindexPlanPOJO reindexPlan = reindexPlanBuilder.generatePlanFromReprocessing(entry.getValue());

                    //TODO test
                    projectsReindexPlanMap.put(entry.getKey(), reindexPlan.updateFailedProject(projectsStatus.get(entry.getKey())));
                    failedProjects.put(entry.getKey(), reindexPlan);
                });
    }

    public synchronized void writeStatusToFile(final String projectId) {
        GeneralUtils.createFile(appSettings.getInternals().getProjectsFolder() + projectId + FILE_SEPARATOR +
                        EAppSettings.PROJECT_MONITORING_FILE.getStringValueOfSetting(),
                projectsStatus.get(projectId));
    }

    public Map<String, ReindexRunner> getProjectsExecutors() {
        return projectsExecutors;
    }

    public void setProjectsExecutors(Map<String, ReindexRunner> projectsExecutors) {
        this.projectsExecutors = projectsExecutors;
    }

    public Map<String, ProjectPOJO> getProjectsMap() {
        return projectsMap;
    }

    public void setProjectsMap(Map<String, ProjectPOJO> projectsMap) {
        this.projectsMap = projectsMap;
    }

    public Map<String, ReindexPlanPOJO> getProjectsReindexPlanMap() {
        return projectsReindexPlanMap;
    }

    public void setProjectsReindexPlanMap(Map<String, ReindexPlanPOJO> projectsReindexPlanMap) {
        this.projectsReindexPlanMap = projectsReindexPlanMap;
    }

    public Map<String, ProjectStatusPOJO> getProjectsStatus() {
        return projectsStatus;
    }

    public void setProjectsStatus(Map<String, ProjectStatusPOJO> projectsStatus) {
        this.projectsStatus = projectsStatus;
    }

    public AppSettingsPOJO getAppSettings() {
        return appSettings;
    }

    public void setAppSettings(AppSettingsPOJO appSettings) {
        this.appSettings = appSettings;
    }

    public ProjectPOJO getNewProjectTemplate() {
        newProjectTemplate.setProjectId(GeneralUtils.generateNewUID());
        return newProjectTemplate.clone();
    }

    public void setNewProjectTemplate(ProjectPOJO newProjectTemplate) {
        this.newProjectTemplate = newProjectTemplate;
    }

    private void readExistingProject(final Path file) {
        if (file.toString().contains(EAppSettings.PROJECT_SETTINGS_FILE.getStringValueOfSetting())) {
            ProjectPOJO project = GeneralUtils.readFileFromFileAndSerializeToObject(file, ProjectPOJO.class);
            if (project != null) {
                projectsMap.put(project.getProjectId(), project);
            }
        } else if (file.toString().contains(EAppSettings.PROJECT_MONITORING_FILE.getStringValueOfSetting())) {
            ProjectStatusPOJO projectStatus = GeneralUtils.readFileFromFileAndSerializeToObject(file, ProjectStatusPOJO.class);
            if (projectStatus != null && projectStatus.isInActiveProcess()) {
                projectStatus.setStatus(EProjectStatus.FAILED);
                projectStatus.setFailed(true);
                projectStatus.setInActiveProcess(false);
                projectStatus.getReindexJobsStatus().stream()
                        .filter(ReindexJobStatusPOJO::isInActiveProcess)
                        .forEach(job -> {
                            job.setStatus(EProjectStatus.FAILED);
                            job.setFailed(true);
                            job.setInActiveProcess(false);
                            job.getReindexTasks().stream()
                                    .filter(ReindexTaskStatusPOJO::isInActiveProcess)
                                    .forEach(task -> {
                                        task.setStatus(EProjectStatus.FAILED);
                                        task.setFailed(true);
                                        task.setInActiveProcess(false);
                                    });
                        });
            }
            projectsStatus.put(projectStatus.getProjectId(), projectStatus);
        }
    }
}
