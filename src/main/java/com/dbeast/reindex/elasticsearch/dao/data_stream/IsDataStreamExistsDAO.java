package com.dbeast.reindex.elasticsearch.dao.data_stream;

import com.dbeast.reindex.elasticsearch.dao.IClusterTaskDAO;
import com.dbeast.reindex.reindex_execution_plan_monitoring.ClusterTaskStatusPOJO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.ElasticsearchStatusException;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.GetDataStreamRequest;
import org.elasticsearch.client.indices.GetDataStreamResponse;
import org.elasticsearch.client.indices.GetIndexRequest;

import java.io.IOException;
import java.util.Arrays;

public class IsDataStreamExistsDAO implements IClusterTaskDAO {
    protected static final Logger logger = LogManager.getLogger();

    private final String dataStream;
    private final GetDataStreamRequest request;

    public IsDataStreamExistsDAO(String dataStream) {
        this.dataStream = dataStream;
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

    protected GetDataStreamRequest generateRequest() {
        return new GetDataStreamRequest(dataStream);
    }

    protected boolean executeRequest(final GetDataStreamRequest request,
                                     final RestHighLevelClient client) {
        try {
            GetDataStreamResponse response =  client.indices().getDataStream(request, RequestOptions.DEFAULT);
            return !response.getDataStreams().isEmpty();
        } catch (IOException e) {
            logger.error("Suppressed: " + Arrays.toString(e.getSuppressed()));
            return false;
        }
    }

    protected GetDataStreamRequest getRequest() {
        return this.request;
    }
}
