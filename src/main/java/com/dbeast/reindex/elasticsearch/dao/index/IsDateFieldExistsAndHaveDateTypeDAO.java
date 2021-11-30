package com.dbeast.reindex.elasticsearch.dao.index;

import com.dbeast.reindex.elasticsearch.DataPeriodFromEs;
import com.dbeast.reindex.elasticsearch.ElasticsearchController;
import com.dbeast.reindex.elasticsearch.dao.IClusterTaskDAO;
import com.dbeast.reindex.reindex_execution_plan_monitoring.ClusterTaskStatusPOJO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.ElasticsearchStatusException;
import org.elasticsearch.client.RestHighLevelClient;

public class IsDateFieldExistsAndHaveDateTypeDAO implements IClusterTaskDAO {
    protected static final Logger logger = LogManager.getLogger();

    private final String index;
    private final String dateField;
    private final ElasticsearchController request;

    public IsDateFieldExistsAndHaveDateTypeDAO(String index, String dateField) {
        this.index = index;
        this.dateField = dateField;
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

    protected ElasticsearchController generateRequest() {
        return new ElasticsearchController();
    }

    protected boolean executeRequest(final ElasticsearchController request,
                                     final RestHighLevelClient client) {
        DataPeriodFromEs date = request.getStartAndEndDateOfIndex(client, index, dateField);
        return date.getDataStartDate() >= 0 && date.getDataEndDate() >= 0 && date.getDataEndDate() < 2003629646000L;
    }

    protected ElasticsearchController getRequest() {
        return this.request;
    }
}
