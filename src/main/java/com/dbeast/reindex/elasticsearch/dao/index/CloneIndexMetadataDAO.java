package com.dbeast.reindex.elasticsearch.dao.index;

import com.dbeast.reindex.elasticsearch.dao.AClusterTaskDAO;
import com.dbeast.reindex.elasticsearch.dao.IClusterTaskDAO;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.client.indices.GetIndexResponse;
import org.elasticsearch.cluster.metadata.MappingMetadata;
import org.elasticsearch.common.settings.Settings;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class CloneIndexMetadataDAO extends AClusterTaskDAO<List<IClusterTaskDAO>> implements IClusterTaskDAO {
    private final List<IClusterTaskDAO> request;

    public CloneIndexMetadataDAO(final String requestType,
                                 final String... params) {
        super(requestType, params);
        this.request = generateRequest();
    }

    @Override
    protected List<IClusterTaskDAO> generateRequest() {
        return new LinkedList<>();
    }

    @Override
    protected boolean executeRequest(final List<IClusterTaskDAO> request,
                                     final RestHighLevelClient client) {
        try {
            GetIndexRequest getRequest = new GetIndexRequest(params[0]);
            GetIndexResponse getIndexResponse = client.indices().get(getRequest, RequestOptions.DEFAULT);
            MappingMetadata indexMappings = getIndexResponse.getMappings().get(params[0]);
            Map<String, Object> indexTypeMappings = indexMappings.getSourceAsMap();
            Settings indexSettings = getIndexResponse.getSettings().get(params[0]);
            CreateIndexRequest createIndexRequest = new CreateIndexRequest(params[1]);
            createIndexRequest.mapping(indexTypeMappings);

            Settings.Builder newSettings = Settings.builder();


            newSettings.put("index.number_of_replicas",indexSettings.get("index.number_of_replicas"));
            newSettings.put("index.number_of_shards",indexSettings.get("index.number_of_shards"));
            createIndexRequest.settings(indexSettings);
            CreateIndexResponse createIndexResponse = client.indices().create(createIndexRequest, RequestOptions.DEFAULT);
            return createIndexResponse.isAcknowledged();
        } catch (IOException e) {
            logger.error("Suppressed: " + Arrays.toString(e.getSuppressed()));
            return false;
        }
    }

    @Override
    protected List<IClusterTaskDAO> getRequest() {
        return request;
    }
}
