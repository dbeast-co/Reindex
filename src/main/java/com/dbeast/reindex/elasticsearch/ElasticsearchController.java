package com.dbeast.reindex.elasticsearch;

import com.dbeast.reindex.exceptions.ClusterConnectionException;
import com.dbeast.reindex.project_settings.EsSettings;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.ElasticsearchStatusException;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthRequest;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthResponse;
import org.elasticsearch.action.admin.indices.settings.get.GetSettingsRequest;
import org.elasticsearch.action.admin.indices.settings.get.GetSettingsResponse;
import org.elasticsearch.action.admin.indices.settings.put.UpdateSettingsRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.*;
import org.elasticsearch.client.core.CountRequest;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.metrics.Max;
import org.elasticsearch.search.aggregations.metrics.MaxAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.Min;
import org.elasticsearch.search.aggregations.metrics.MinAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class ElasticsearchController {
    private static final Logger logger = LogManager.getLogger();
    private final ObjectMapper mapper = new ObjectMapper();
    private final ElasticsearchDbProvider elasticsearchClient = new ElasticsearchDbProvider();

    public boolean createIndex(final RestHighLevelClient client,
                               final String index) {
        //Check is index exists
        CreateIndexRequest request = new CreateIndexRequest(index);
        try {
            CreateIndexResponse createIndexResponse = client.indices().create(request, RequestOptions.DEFAULT);
            return createIndexResponse.isAcknowledged();
        } catch (IOException | ElasticsearchStatusException e) {
            logger.error(e);
            return false;
        } catch (RuntimeException e) {
            logger.warn(e);
            return false;
        }
    }

    public int getNumberOfReplicasFromIndex(final RestHighLevelClient client,
                                            final String index) {
        GetSettingsRequest request = new GetSettingsRequest().indices(index);
        try {
            GetSettingsResponse getSettingsResponse = client.indices().getSettings(request, RequestOptions.DEFAULT);
            if (getSettingsResponse != null) {
                return getSettingsResponse.getIndexToSettings().get(index).getAsInt("index.number_of_replicas", 0);
            } else {
                return -1;
            }
        } catch (IOException | ElasticsearchStatusException e) {
            logger.error(e);
            return -1;
        } catch (RuntimeException e) {
            logger.warn(e);
            return -1;
        }
    }


    public boolean updateNumberOfReplicas(final RestHighLevelClient client,
                                          final String index,
                                          final int numberOfReplicas) {
        UpdateSettingsRequest request = new UpdateSettingsRequest(index);
        String settingKey = "index.number_of_replicas";
        Settings settings = Settings.builder()
                .put(settingKey, numberOfReplicas)
                .build();
        request.settings(settings);
        try {
            AcknowledgedResponse updateSettingsResponse =
                    client.indices().putSettings(request, RequestOptions.DEFAULT);
            return updateSettingsResponse.isAcknowledged();
        } catch (IOException | ElasticsearchStatusException e) {
            logger.error(e);
            return false;
        } catch (RuntimeException e) {
            logger.warn(e);
            return false;
        }
    }

    public long getIndexDocsCount(final EsSettings connectionSettings,
                                  final String projectId,
                                  final String index) throws ClusterConnectionException {
        RestHighLevelClient client = elasticsearchClient.getHighLevelClient(connectionSettings, projectId);
        CountRequest countRequest = new CountRequest(index);
        try {
            return client
                    .count(countRequest, RequestOptions.DEFAULT).getCount();
        } catch (IOException | ElasticsearchStatusException e) {
            logger.error(e);
            return 0;
        } catch (RuntimeException e) {
            logger.warn(e);
            return 0;
        } finally {
            try {
                if (client != null) {
                    client.close();
                }
            } catch (IOException e) {
                logger.error("Can't close the client in get documents count. Exception: " + e);
            }
        }
    }

    public String getTemplateParameters(final EsSettings connectionSettings,
                                        final String template,
                                        final String projectId) throws ClusterConnectionException {
        RestClient client = elasticsearchClient.getLowLevelClient(connectionSettings, projectId);
        Response response;
        try {
            response = client.performRequest(new Request("GET", "_template/" + template));
            if (response.getStatusLine().getStatusCode() == 200) {
                // parse the JSON response
                return EntityUtils.toString(response.getEntity());
            } else {
                logger.error("There is the error in the get index parameters of index: " + template);
            }
        } catch (IOException e) {
            logger.error(e);
            return String.valueOf(e);
        } finally {
            try {
                if (client != null) {
                    client.close();
                }
            } catch (IOException e) {
                logger.error("Can't close the client in the get templates. Exception: " + e);
            }
        }
        return null;
    }

    public String getIndexParameters(final EsSettings connectionSettings,
                                     final String index,
                                     final String projectId) throws ClusterConnectionException {
        RestClient client = elasticsearchClient.getLowLevelClient(connectionSettings, projectId);
        Response response;
        try {
            response = client.performRequest(new Request("GET", index));
            if (response.getStatusLine().getStatusCode() == 200) {
                return EntityUtils.toString(response.getEntity());
            } else {
                logger.error("There is the error in the get index parameters of index: " + index);
            }
        } catch (IOException e) {
            logger.error(e);
            return String.valueOf(e);
        } finally {
            try {
                if (client != null) {
                    client.close();
                }
            } catch (IOException e) {
                logger.error("Can't close the client in the get index list. Exception: " + e);
            }
        }
        return null;
    }

    public List<HashMap<String, String>> getIndexList(final EsSettings connectionSettings,
                                                      final String projectId) throws ClusterConnectionException {
        return getTemplateOrIndexList(connectionSettings, "/_cat/indices?h=index&format=json", projectId);
    }

    public List<HashMap<String, String>> getTemplateList(final EsSettings connectionSettings,
                                                         final String projectId) throws ClusterConnectionException {
        return getTemplateOrIndexList(connectionSettings, "/_cat/templates?h=name&format=json", projectId);
    }

    public String getClusterStatus(final EsSettings connectionSettings,
                                   final String projectId) throws Exception {
        RestHighLevelClient client = elasticsearchClient.getHighLevelClient(connectionSettings, projectId);
        try {
            ClusterHealthRequest request = new ClusterHealthRequest();
            request.timeout("30s");
            ClusterHealthResponse response = client.cluster().health(request, RequestOptions.DEFAULT);
            return response.getStatus().toString();
        } catch (Exception e) {
            throw e;
        } finally {
            try {
                if (client != null) {
                    client.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public DataPeriodFromEs getStartAndEndDateOfIndex(final RestHighLevelClient client,
                                                      final String index,
                                                      final String dateField) {
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.indices(index);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        MaxAggregationBuilder maxAggregation = AggregationBuilders.max("max").field(dateField);
        MinAggregationBuilder minAggregation = AggregationBuilders.min("min").field(dateField);
        searchSourceBuilder.aggregation(maxAggregation);
        searchSourceBuilder.aggregation(minAggregation);
        searchSourceBuilder.size(0);
        searchRequest.source(searchSourceBuilder);
        try {
            SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
            Aggregations aggregations = searchResponse.getAggregations();
            Min min = aggregations.get("min");
            Max max = aggregations.get("max");
            if (searchResponse.getHits().getTotalHits().value == 0) {
                return new DataPeriodFromEs(
                        0, 0
                );
            } else {
                return new DataPeriodFromEs(
                        new Double(min.getValue()).longValue(),
                        new Double(max.getValue()).longValue()
                );
            }

        } catch (IOException | ElasticsearchException e) {
//            e.printStackTrace();
            logger.error(e);
            //TODO add index is empty exception
            return new DataPeriodFromEs(-1, -1);
        }
    }

    //TODO add catch exception
    private List<HashMap<String, String>> getTemplateOrIndexList(final EsSettings connectionSettings,
                                                                 final String endPoint,
                                                                 final String projectId) throws ClusterConnectionException {
        Response response;
        RestClient client = elasticsearchClient.getLowLevelClient(connectionSettings, projectId);
        try {
            response = client.performRequest(new Request("GET", endPoint));
            if (response.getStatusLine().getStatusCode() == 200) {
                // parse the JSON response
                String rawBody = EntityUtils.toString(response.getEntity());
                TypeReference<List<HashMap<String, String>>> typeRef = new TypeReference<List<HashMap<String, String>>>() {
                };
                return mapper.readValue(rawBody, typeRef);
            }
        } catch (IOException e) {
            logger.error(e.getMessage());
        } finally {
            try {
                if (client != null) {
                    client.close();
                }
            } catch (IOException e) {
                logger.error("Can't close the client in the get index list. Exception: " + e);
            }
        }
        return new LinkedList<>();
    }
}
