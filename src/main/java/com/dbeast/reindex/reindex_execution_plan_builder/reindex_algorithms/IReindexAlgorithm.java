package com.dbeast.reindex.reindex_execution_plan_builder.reindex_algorithms;

import com.dbeast.reindex.exceptions.ClusterConnectionException;
import com.dbeast.reindex.project_settings.EsSettings;
import com.dbeast.reindex.reindex_execution_plan_builder.reindex_plan.ReindexTaskPOJO;

import java.util.List;
import java.util.Map;

public interface IReindexAlgorithm {

    List<ReindexTaskPOJO> generateRequests(final EsSettings destination,
                                           final String index,
                                           final boolean isRemote,
                                           final String projectId) throws ClusterConnectionException;

    boolean isSelected();

    String getReindexAlgorithmName();

    ReindexTaskPOJO generateRequestFromReprocessing(final EsSettings source,
                                                    final String index,
                                                    final boolean isRemote,
                                                    final Map<String, Object> reprocessingParams);

}
