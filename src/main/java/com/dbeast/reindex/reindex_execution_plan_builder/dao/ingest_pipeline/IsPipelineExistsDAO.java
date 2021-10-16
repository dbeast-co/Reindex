package com.dbeast.reindex.reindex_execution_plan_builder.dao.ingest_pipeline;

import com.dbeast.reindex.reindex_execution_plan_builder.dao.IClusterTaskDAO;
import com.dbeast.reindex.reindex_execution_plan_monitoring.ClusterTaskStatusPOJO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.ElasticsearchStatusException;
import org.elasticsearch.action.ingest.GetPipelineRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;

import java.io.IOException;
import java.util.Arrays;

public class IsPipelineExistsDAO implements IClusterTaskDAO {
    protected static final Logger logger = LogManager.getLogger();

    private final String pipeline;
    private final GetPipelineRequest request;

    public IsPipelineExistsDAO(String pipeline) {
        this.pipeline = pipeline;
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

    protected GetPipelineRequest generateRequest() {
        return new GetPipelineRequest(pipeline);
    }

    protected boolean executeRequest(final GetPipelineRequest request,
                                     final RestHighLevelClient client) {
        try {
            return client.ingest().getPipeline(request, RequestOptions.DEFAULT).isFound();
        } catch (IOException e) {
            logger.error("Suppressed: " + Arrays.toString(e.getSuppressed()));
            return false;
        }
    }

    protected GetPipelineRequest getRequest() {
        return this.request;
    }
}
