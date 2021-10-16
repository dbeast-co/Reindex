package com.dbeast.reindex.reindex_execution_plan_builder.dao;

import com.dbeast.reindex.reindex_execution_plan_monitoring.ClusterTaskStatusPOJO;
import org.elasticsearch.client.RestHighLevelClient;

public interface IClusterTaskDAO{

    boolean execute(final RestHighLevelClient client, final ClusterTaskStatusPOJO status);

    String getDescription();

    String getSelfGeneratedTaskId();
}
