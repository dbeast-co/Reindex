package com.dbeast.reindex.elasticsearch.dao.alias;

import com.dbeast.reindex.elasticsearch.dao.IClusterTaskDAO;
import com.dbeast.reindex.reindex_execution_plan_monitoring.ClusterTaskStatusPOJO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.ElasticsearchStatusException;
import org.elasticsearch.action.admin.indices.alias.get.GetAliasesRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;

import java.io.IOException;
import java.util.Arrays;

public class IsAliasExistsDAO implements IClusterTaskDAO {
    protected static final Logger logger = LogManager.getLogger();

    private final String alias;
    private final GetAliasesRequest request;

    public IsAliasExistsDAO(String alias) {
        this.alias = alias;
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

    protected GetAliasesRequest generateRequest() {
        return new GetAliasesRequest(alias);
    }

    protected boolean executeRequest(final GetAliasesRequest request,
                                     final RestHighLevelClient client) {
        try {
            return client.indices().existsAlias(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            logger.error("Suppressed: " + Arrays.toString(e.getSuppressed()));
            return false;
        }
    }

    protected GetAliasesRequest getRequest() {
        return this.request;
    }
}

