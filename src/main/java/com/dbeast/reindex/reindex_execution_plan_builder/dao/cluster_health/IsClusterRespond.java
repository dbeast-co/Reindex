package com.dbeast.reindex.reindex_execution_plan_builder.dao.cluster_health;

import com.dbeast.reindex.reindex_execution_plan_builder.dao.IClusterTaskDAO;
import com.dbeast.reindex.reindex_execution_plan_monitoring.ClusterTaskStatusPOJO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.ElasticsearchStatusException;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthRequest;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.cluster.health.ClusterHealthStatus;

import java.io.IOException;
import java.net.ConnectException;

public class IsClusterRespond implements IClusterTaskDAO {
    protected static final Logger logger = LogManager.getLogger();

    private final ClusterHealthRequest request;

    public IsClusterRespond() {
        this.request = generateRequest();
    }

    @Override
    public boolean execute(final RestHighLevelClient client,
                           final ClusterTaskStatusPOJO status) {
        try {
            return executeRequest(getRequest(), client);
        } catch (ElasticsearchStatusException e) {
            logger.error(e.getDetailedMessage());
            return false;
        }
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public String getSelfGeneratedTaskId() {
        return null;
    }

    protected ClusterHealthRequest generateRequest() {
        return new ClusterHealthRequest();
    }

    protected boolean executeRequest(final ClusterHealthRequest request,
                                     final RestHighLevelClient client) {
        try {
            ClusterHealthResponse response = client.cluster().health(request, RequestOptions.DEFAULT);
            ClusterHealthStatus status = response.getStatus();
            return !status.equals(ClusterHealthStatus.RED);
        } catch (ConnectException e){
            logger.error("We can't connect to the cluster! Error: " + e);
            return false;
        }
        catch (IOException e) {
            logger.error("There is an error in the cluster health test! Error: " + e);
            return false;
        }
    }

    protected ClusterHealthRequest getRequest() {
        return this.request;
    }
}
