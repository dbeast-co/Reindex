package com.dbeast.reindex.reindex_execution_plan_builder.reindex_algorithms;

import com.dbeast.reindex.constants.EReindexAlgorithms;
import com.dbeast.reindex.elasticsearch.DataPeriodFromEs;
import com.dbeast.reindex.elasticsearch.ElasticsearchController;
import com.dbeast.reindex.elasticsearch.ElasticsearchDbProvider;
import com.dbeast.reindex.exceptions.ClusterConnectionException;
import com.dbeast.reindex.project_settings.ESHostPOJO;
import com.dbeast.reindex.project_settings.EsSettings;
import com.dbeast.reindex.utils.GeneralUtils;
import com.dbeast.reindex.reindex_execution_plan_builder.reindex_plan.ReindexTaskPOJO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.bytes.BytesArray;
import org.elasticsearch.core.TimeValue;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.index.reindex.ReindexRequest;
import org.elasticsearch.index.reindex.RemoteInfo;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class TimeSeriesReindexAlgorithm extends ReindexAlgorithmPOJO implements IReindexAlgorithm {
    private static final Logger logger = LogManager.getLogger();

    public TimeSeriesReindexAlgorithm(final boolean isNew) {
        super(EReindexAlgorithms.TIME_ORIENTED.getVarForUI(),
                Arrays.asList(
                        new AlgorithmParam("Date field", "input", "@timestamp", "@timestamp"),
                        new AlgorithmParam("Time frame", "input", 60, 60),
                        new AlgorithmParam("Date format", "input", "yyyy-MM-dd'T'HH:mm:ss.SSSZZ", "yyyy-MM-dd'T'HH:mm:ss.SSSZZ")
                ),
                true
        );
    }

    public TimeSeriesReindexAlgorithm(final ReindexAlgorithmPOJO reindexAlgorithm) {
        super(reindexAlgorithm);
    }

    public TimeSeriesReindexAlgorithm() {
        isSelected = true;
    }

    private String getCurrentDateField() {
        return algorithmParams.stream()
                .filter(x -> x.getLabel().equals("Date field"))
                .map(AlgorithmParam::getActualValue)
                .collect(Collectors.toList())
                .get(0).toString();
    }

    private long getCurrentTimeChunk() {
        String stringTimeChunk = algorithmParams.stream()
                .filter(x -> x.getLabel().equals("Time frame"))
                .map(AlgorithmParam::getActualValue)
                .collect(Collectors.toList())
                .get(0).toString();
        return Integer.parseInt(stringTimeChunk) * 1000 * 60;
    }

    private String getCurrentDateFormat() {
        return algorithmParams.stream()
                .filter(x -> x.getLabel().equals("Date format"))
                .map(AlgorithmParam::getActualValue)
                .collect(Collectors.toList())
                .get(0).toString();

    }

    public ReindexTaskPOJO generateRequestFromReprocessing(final EsSettings source,
                                                           final String index,
                                                           final boolean isRemote,
                                                           final Map<String, Object> reprocessingParams) {

        String dateField = String.valueOf(reprocessingParams.get("date_field"));
        String dateFormatFromParams = String.valueOf(reprocessingParams.get("date_format"));
        SimpleDateFormat dateFormat = new SimpleDateFormat(dateFormatFromParams);
        long fromDate = (Long) reprocessingParams.get("from_date");
        long toDate = (Long) reprocessingParams.get("to_date");

        ReindexTaskPOJO reindexTask = new ReindexTaskPOJO(index);
        reindexTask.addReprocessParam("from_date", reprocessingParams.get("from_date"));
        reindexTask.addReprocessParam("to_date", reprocessingParams.get("to_date"));
        reindexTask.addReprocessParam("date_field", reprocessingParams.get("date_field"));
        reindexTask.addReprocessParam("date_format", reprocessingParams.get("date_format"));

        ReindexRequest request;
        if (isRemote) {
            request = buildRemoteRequest(source,
                    dateField,
                    GeneralUtils.convertLongToDateString(fromDate, dateFormat),
                    GeneralUtils.convertLongToDateString(toDate, dateFormat));
        } else {
            request = buildRequest(dateField,
                    GeneralUtils.convertLongToDateString(fromDate, dateFormat),
                    GeneralUtils.convertLongToDateString(toDate, dateFormat));
        }
        reindexTask.setReindexRequest(request);
        reindexTask.setReindexParams("From: " + GeneralUtils.convertLongToDateString(fromDate, dateFormat) +
                " To: " + GeneralUtils.convertLongToDateString(toDate, dateFormat));

        return reindexTask;
    }

    @Override
    public List<ReindexTaskPOJO> generateRequests(final EsSettings source,
                                                  final String index,
                                                  final boolean isRemote,
                                                  final String projectId) throws ClusterConnectionException {
        List<ReindexTaskPOJO> reindexTasks = new LinkedList<>();
        ElasticsearchDbProvider elasticsearchDbProvider = new ElasticsearchDbProvider();
        RestHighLevelClient client = elasticsearchDbProvider.getHighLevelClient(source, projectId);
        ElasticsearchController elasticsearchController = new ElasticsearchController();

        String dateField = getCurrentDateField();
        String dateFormatFromParams = getCurrentDateFormat();
        long timeChunk = getCurrentTimeChunk();

        SimpleDateFormat dateFormat = new SimpleDateFormat(dateFormatFromParams);

        DataPeriodFromEs starAnEndDate = elasticsearchController.getStartAndEndDateOfIndex(client, index, dateField);
        if (starAnEndDate.getDataEndDate() == -1) {
            return new LinkedList<>();
        }

        long fromDate = starAnEndDate.getDataStartDate();
        long toDate = starAnEndDate.getDataEndDate();


        ReindexRequest request;
        do {
            ReindexTaskPOJO reindexTask = new ReindexTaskPOJO(index);
            reindexTask.addReprocessParam("from_date", fromDate);
            reindexTask.addReprocessParam("to_date", fromDate + timeChunk);
            reindexTask.addReprocessParam("date_field", dateField);
            reindexTask.addReprocessParam("date_format", dateFormatFromParams);
            if (isRemote) {
                request = buildRemoteRequest(source,
                        dateField,
                        GeneralUtils.convertLongToDateString(fromDate, dateFormat),
                        GeneralUtils.convertLongToDateString(fromDate + timeChunk, dateFormat));
            } else {
                request = buildRequest(dateField,
                        GeneralUtils.convertLongToDateString(fromDate, dateFormat),
                        GeneralUtils.convertLongToDateString(fromDate + timeChunk, dateFormat));
            }
            reindexTask.setReindexRequest(request);
            reindexTask.setReindexParams("From: " + GeneralUtils.convertLongToDateString(fromDate, dateFormat) +
                    " To: " + GeneralUtils.convertLongToDateString((fromDate + timeChunk), dateFormat));

            reindexTasks.add(reindexTask);
            fromDate += timeChunk;
        }
        while (fromDate < toDate);
        try {
            client.close();
        } catch (IOException e) {
            logger.error("Can't close the client. Exception: " + e);
        }
        return reindexTasks;
    }

    private ReindexRequest buildRequest(final String dateField,
                                        final String startDate,
                                        final String endDate) {
        ReindexRequest request = new ReindexRequest();
        RangeQueryBuilder query = new RangeQueryBuilder(dateField).gte(startDate).lt(endDate);
        request.setSourceQuery(query);
        return request;
    }

    private ReindexRequest buildRemoteRequest(final EsSettings source,
                                              final String dateField,
                                              final String startDate,
                                              final String endDate) {
        ReindexRequest request = new ReindexRequest();
        ESHostPOJO esHost = new ESHostPOJO(source);
        request.setRemoteInfo(
                new RemoteInfo(
                        esHost.getProtocol(),
                        esHost.getDomain(),
                        esHost.getPort(),
                        null,
                        new BytesArray(new RangeQueryBuilder(dateField).gte(startDate).lt(endDate).toString()),
                        source.getUsername(), source.getPassword(), Collections.emptyMap(),
                        new TimeValue(100, TimeUnit.MILLISECONDS),
                        new TimeValue(100, TimeUnit.SECONDS)
                ));
        return request;
    }

    @Override
    public String toString() {
        return "TimeSeriesReindexAlgorithm{" +
                "reindexAlgorithmName='" + reindexAlgorithmName + '\'' +
                ", algorithmParams=" + algorithmParams +
                ", isChecked=" + isSelected +
                '}';
    }
}


