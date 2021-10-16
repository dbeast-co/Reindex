package com.dbeast.reindex.reindex_execution_plan_builder.dao.cluster_settings;

import com.dbeast.reindex.reindex_execution_plan_builder.dao.IClusterTaskDAO;
import com.dbeast.reindex.reindex_execution_plan_monitoring.ClusterTaskStatusPOJO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.ElasticsearchStatusException;
import org.elasticsearch.action.admin.cluster.settings.ClusterGetSettingsRequest;
import org.elasticsearch.action.admin.cluster.settings.ClusterGetSettingsResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IsReindexRemoteClusterDefined implements IClusterTaskDAO {
    protected static final Logger logger = LogManager.getLogger();
    private final String host;
    private final ClusterGetSettingsRequest request;

    public IsReindexRemoteClusterDefined(final String host) {
        this.request = generateRequest();
        this.host = host;
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

    protected ClusterGetSettingsRequest generateRequest() {
        ClusterGetSettingsRequest clusterSettingsRequest = new ClusterGetSettingsRequest();
        clusterSettingsRequest.includeDefaults(true);
        return clusterSettingsRequest;
    }

    protected boolean executeRequest(final ClusterGetSettingsRequest request,
                                     final RestHighLevelClient client) {
        try {
            Pattern pattern = Pattern.compile("(https?)://(.*)");
            Matcher matcher = pattern.matcher(host);
            matcher.find();
            String domain = matcher.group(2);
            ClusterGetSettingsResponse response = client.cluster().getSettings(request, RequestOptions.DEFAULT);
            List<String> remoteWhitelist = response.getDefaultSettings().getAsSettings("reindex").getAsSettings("remote").getAsList("whitelist");
            if (remoteWhitelist.size() > 0) {
                return remoteWhitelist.stream()
                        .map(raw -> raw.replace("*", ".*").replace("?", ".?"))
                        .anyMatch(raw -> Pattern.compile(raw).matcher(domain).matches());
            } else {
                return false;
            }
        } catch (IOException e) {
            logger.error("Suppressed: " + Arrays.toString(e.getSuppressed()));
            return false;
        }
    }

    protected ClusterGetSettingsRequest getRequest() {
        return this.request;
    }
}
