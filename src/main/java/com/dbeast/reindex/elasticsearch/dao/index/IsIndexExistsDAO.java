package com.dbeast.reindex.elasticsearch.dao.index;

import com.dbeast.reindex.elasticsearch.dao.IClusterTaskDAO;
import com.dbeast.reindex.reindex_execution_plan_monitoring.ClusterTaskStatusPOJO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.ElasticsearchStatusException;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.GetIndexRequest;

import java.io.IOException;
import java.util.Arrays;

public class IsIndexExistsDAO implements IClusterTaskDAO {
    protected static final Logger logger = LogManager.getLogger();

    private final String index;
    private final GetIndexRequest request;

    public IsIndexExistsDAO(String index) {
        this.index = index;
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

    protected GetIndexRequest generateRequest() {
        return new GetIndexRequest(index);
    }

    protected boolean executeRequest(final GetIndexRequest request,
                                     final RestHighLevelClient client) {
        try {
            return !client.indices().exists(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            logger.error("Suppressed: " + Arrays.toString(e.getSuppressed()));
            return false;
        }
    }

    protected GetIndexRequest getRequest() {
        return this.request;
    }
}
