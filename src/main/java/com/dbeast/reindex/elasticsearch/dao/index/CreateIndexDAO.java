package com.dbeast.reindex.elasticsearch.dao.index;

import com.dbeast.reindex.elasticsearch.dao.AClusterTaskDAO;
import com.dbeast.reindex.elasticsearch.dao.IClusterTaskDAO;
import org.elasticsearch.action.admin.indices.alias.Alias;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;

import java.io.IOException;
import java.util.Arrays;

public class CreateIndexDAO extends AClusterTaskDAO<CreateIndexRequest> implements IClusterTaskDAO {
    private final CreateIndexRequest request;

    public CreateIndexDAO(final String requestType,
                          final String... params) {
        super(requestType,params);
        this.request = generateRequest();
    }

    @Override
    protected boolean executeRequest(final CreateIndexRequest request,
                                     final RestHighLevelClient client) {
        try {
            CreateIndexResponse createIndexResponse = client.indices().create(request, RequestOptions.DEFAULT);
            return createIndexResponse.isAcknowledged();
        } catch (IOException e) {
            logger.error("Suppressed: " + Arrays.toString(e.getSuppressed()));
            return false;
        }
    }

    @Override
    public CreateIndexRequest getRequest() {
        return request;
    }

    @Override
    protected CreateIndexRequest generateRequest() {
        switch (requestType) {
            case "create index with alias": {
                return createWritenIndexWithAliasRequest(params[0], params[1]);
            }
            case "create index": {
                return createIndexRequest(params[0]);
            }
            default: {
                return new CreateIndexRequest(params[0]);
            }
        }
    }

    private CreateIndexRequest createWritenIndexWithAliasRequest(final String alias,
                                                                 final String rolloverIndex) {
        CreateIndexRequest request = new CreateIndexRequest(rolloverIndex);
        request.alias(new Alias(alias).writeIndex(true));
        this.description = "Create index: " + rolloverIndex + " and connect it to alias: " + alias;
        return request;
    }

    private CreateIndexRequest createIndexRequest(final String index) {
        this.description = "Create index: " + index;
        return new CreateIndexRequest(index);
    }
}
