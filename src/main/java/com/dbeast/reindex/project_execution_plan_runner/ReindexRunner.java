package com.dbeast.reindex.project_execution_plan_runner;

import com.dbeast.reindex.reindex_execution_plan_builder.reindex_plan.ReindexPlanPOJO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ReindexRunner {
    private static final Logger logger = LogManager.getLogger();
    private final long taskRefreshInterval;
    private final int tasksAPIRetriesNumber;


    private Thread thread;
    private String projectId;
    private ReindexProjectExecutor reindexProjectExecutor;

    public ReindexRunner(final long taskRefreshInterval,
                         final int tasksAPIRetriesNumber) {
        this.taskRefreshInterval = taskRefreshInterval;
        this.tasksAPIRetriesNumber = tasksAPIRetriesNumber;
    }

    public boolean executePlan(final ReindexPlanPOJO plan) {
        this.projectId = plan.getProjectId();
        try {
            reindexProjectExecutor = new ReindexProjectExecutor(plan, taskRefreshInterval, tasksAPIRetriesNumber );
            thread = new Thread(reindexProjectExecutor);
            thread.start();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void stop() {
        logger.warn("The project: " + projectId + " asked to be interrupted");
        reindexProjectExecutor.stop();
        thread.interrupt();
    }

}
