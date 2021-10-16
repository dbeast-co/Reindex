package com.dbeast.reindex.reindex_execution_plan_builder.plan_validation;

import com.dbeast.reindex.elasticsearch.ElasticsearchDbProvider;
import com.dbeast.reindex.exceptions.ClusterConnectionException;
import com.dbeast.reindex.reindex_execution_plan_monitoring.ClusterTaskStatusPOJO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.client.RestHighLevelClient;

import java.io.IOException;

public class ValidationPlanExecutor {
    private static final Logger logger = LogManager.getLogger();

    private final ValidationPlanPOJO validationPlan;
    private final RestHighLevelClient destinationClient;
    private final ValidationResponsePOJO validationResponse;

    public ValidationPlanExecutor(final ValidationPlanPOJO validationPlan) throws ClusterConnectionException {
        this.validationPlan = validationPlan;
        ElasticsearchDbProvider elasticsearchDbProvider = new ElasticsearchDbProvider();
        destinationClient = elasticsearchDbProvider.getHighLevelClient(validationPlan.getConnectionSettings().getDestination(), validationPlan.getProjectId());
        validationResponse = validationPlan.getValidationResponse();
    }

    public ValidationResponsePOJO executePlan() {
        validationPlan.getValidationTasks().forEach(task -> {
                    boolean clusterTaskResult = task.getRequestDAO().execute(destinationClient, new ClusterTaskStatusPOJO());
                    validationResponse.getValidationResults().stream()
                            .filter(validationResult -> validationResult.getValidationParam().equals(task.getValidationParam()))
                            .forEach(validationResult -> validationResult.updateStatus(clusterTaskResult));
                }
        );
        int numericStatus = validationResponse.calculateProjectStatus();
        if (numericStatus == 2) {
            validationResponse.getValidationResults()
                    .removeIf(task -> task.getNumericStatus() < 2);
        }
        if (logger.isDebugEnabled()) {
            logger.debug(validationResponse.toString());
        }
        try {
            if (destinationClient != null) {
                destinationClient.close();
            }
        } catch (IOException e) {
            logger.error("Can't close the client in get documents count. Exception: " + e);
        }
        return validationResponse;
    }


}
