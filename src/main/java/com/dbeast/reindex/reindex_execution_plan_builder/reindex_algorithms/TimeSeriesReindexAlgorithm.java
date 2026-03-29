package com.dbeast.reindex.reindex_execution_plan_builder.reindex_algorithms;

import com.dbeast.reindex.constants.EReindexAlgorithms;
import com.dbeast.reindex.constants.ETimestampFormat;
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

    private String getCurrentTimestampFormat() {
        String dateFormatString = getCurrentDateFormat();

        // Check if it's just the epoch type (e.g., "epoch_millis" or "epoch_nanos")
        if (dateFormatString.equalsIgnoreCase("epoch_millis") ||
            dateFormatString.equalsIgnoreCase("epoch_nanos")) {
            return dateFormatString.toLowerCase();
        }

        // Extract timestamp format from parentheses at end: (epoch_millis) or (epoch_nanos)
        if (dateFormatString.contains("(") && dateFormatString.contains(")")) {
            int startIdx = dateFormatString.lastIndexOf("(");
            int endIdx = dateFormatString.lastIndexOf(")");
            if (startIdx < endIdx) {
                return dateFormatString.substring(startIdx + 1, endIdx).trim();
            }
        }
        // Default to epoch_millis if not specified
        return ETimestampFormat.EPOCH_MILLIS.getVarForUI();
    }

    /**
     * Extracts the SimpleDateFormat pattern from the date format string (removing timestamp type notation)
     * Input: "yyyy-MM-dd'T'HH:mm:ss.SSSZZ (epoch_millis)" → Output: "yyyy-MM-dd'T'HH:mm:ss.SSSZZ"
     * Input: "epoch_millis" → Output: "yyyy-MM-dd'T'HH:mm:ss.SSSZZ" (default pattern)
     * Input: "yyyy-MM-dd" → Output: "yyyy-MM-dd"
     */
    private String getDateFormatPattern() {
        String dateFormatString = getCurrentDateFormat();

        // If it's just the epoch type, return default pattern
        if (dateFormatString.equalsIgnoreCase("epoch_millis") ||
            dateFormatString.equalsIgnoreCase("epoch_nanos")) {
            return "yyyy-MM-dd'T'HH:mm:ss.SSSZZ";  // Default pattern
        }

        // Remove the timestamp format part if present: (epoch_millis) or (epoch_nanos)
        if (dateFormatString.contains("(") && dateFormatString.contains(")")) {
            int idx = dateFormatString.lastIndexOf("(");
            return dateFormatString.substring(0, idx).trim();
        }
        return dateFormatString;
    }

    /**
     * Determines if the date format string specifies an epoch type
     * Returns true if format is just "epoch_millis" or "epoch_nanos" or contains "(epoch_...)"
     * Returns false if format is a regular date pattern like "yyyy-MM-dd"
     */
    private boolean isEpochTypeSpecified(final String dateFormatString) {
        // Check if it's just the epoch type
        if (dateFormatString.equalsIgnoreCase("epoch_millis") ||
            dateFormatString.equalsIgnoreCase("epoch_nanos")) {
            return true;
        }

        // Check if it contains epoch type in parentheses
        if (dateFormatString.contains("(") && dateFormatString.contains(")")) {
            String content = dateFormatString.toLowerCase();
            return content.contains("epoch_millis") || content.contains("epoch_nanos");
        }

        return false;
    }

    /**
     * Determines if the timestamp field contains nanoseconds (true) or milliseconds (false)
     */
    private boolean isNanosTimestamp() {
        String timestampFormat = getCurrentTimestampFormat();
        ETimestampFormat format = ETimestampFormat.fromVarForUI(timestampFormat);
        return format == ETimestampFormat.EPOCH_NANOS;
    }

    /**
                                                           final String index,
                                                           final boolean isRemote,
                                                           final Map<String, Object> reprocessingParams) {

        String dateField = String.valueOf(reprocessingParams.get("date_field"));
        String dateFormatFromParams = String.valueOf(reprocessingParams.get("date_format"));

        // Extract SimpleDateFormat pattern and timestamp format from the date_format string
        String timestampFormat = extractTimestampFormat(dateFormatFromParams);
        String datePattern = extractDatePattern(dateFormatFromParams);
        boolean isEpochType = isEpochTypeSpecified(dateFormatFromParams);

        SimpleDateFormat dateFormat = new SimpleDateFormat(datePattern);
        boolean isNanos = ETimestampFormat.fromVarForUI(timestampFormat) == ETimestampFormat.EPOCH_NANOS;
        long fromDate = (Long) reprocessingParams.get("from_date");
        long toDate = (Long) reprocessingParams.get("to_date");

        ReindexTaskPOJO reindexTask = new ReindexTaskPOJO(index);
        reindexTask.addReprocessParam("from_date", reprocessingParams.get("from_date"));
        reindexTask.addReprocessParam("to_date", reprocessingParams.get("to_date"));
        reindexTask.addReprocessParam("date_field", reprocessingParams.get("date_field"));
        reindexTask.addReprocessParam("date_format", reprocessingParams.get("date_format"));

        ReindexRequest request;
        if (isEpochType) {
            // For epoch timestamps, send raw numeric values
            if (isRemote) {
                request = buildRemoteRequest(source, dateField, fromDate, toDate);
            } else {
                request = buildRequest(dateField, fromDate, toDate);
            }
            reindexTask.setReindexParams("From: " + fromDate + " To: " + toDate);
        } else {
            // For date string format, convert to formatted strings
            if (isRemote) {
                request = buildRemoteRequest(source,
                        dateField,
                        GeneralUtils.convertTimestampToDateString(fromDate, dateFormat, isNanos),
                        GeneralUtils.convertTimestampToDateString(toDate, dateFormat, isNanos));
            } else {
                request = buildRequest(dateField,
                        GeneralUtils.convertTimestampToDateString(fromDate, dateFormat, isNanos),
                        GeneralUtils.convertTimestampToDateString(toDate, dateFormat, isNanos));
            }
            reindexTask.setReindexParams("From: " + GeneralUtils.convertTimestampToDateString(fromDate, dateFormat, isNanos) +
                    " To: " + GeneralUtils.convertTimestampToDateString(toDate, dateFormat, isNanos));
        }

        reindexTask.setReindexRequest(request);
        // Query is automatically extracted and set by setReindexRequest()

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
        String datePattern = getDateFormatPattern();
        long timeChunk = getCurrentTimeChunk();
        boolean isNanos = isNanosTimestamp();
        boolean isEpochType = isEpochTypeSpecified(dateFormatFromParams);

        SimpleDateFormat dateFormat = new SimpleDateFormat(datePattern);

        DataPeriodFromEs starAnEndDate = elasticsearchController.getStartAndEndDateOfIndex(client, index, dateField);
        if (starAnEndDate.getDataEndDate() == -1) {
            return new LinkedList<>();
        }

        long fromDate = starAnEndDate.getDataStartDate();
        long toDate = starAnEndDate.getDataEndDate();

        // If timestamps are in nanos, convert time chunk from millis to nanos
        long adjustedTimeChunk = isNanos ? timeChunk * 1_000_000 : timeChunk;

        ReindexRequest request;
        do {
            ReindexTaskPOJO reindexTask = new ReindexTaskPOJO(index);
            reindexTask.addReprocessParam("from_date", fromDate);
            reindexTask.addReprocessParam("to_date", fromDate + adjustedTimeChunk);
            reindexTask.addReprocessParam("date_field", dateField);
            reindexTask.addReprocessParam("date_format", dateFormatFromParams);

            // If epoch type is specified, send raw numeric values to Elasticsearch
            // Otherwise, send formatted date strings
            if (isEpochType) {
                // For epoch timestamps (millis or nanos), send as Long values
                if (isRemote) {
                    request = buildRemoteRequest(source, dateField, fromDate, fromDate + adjustedTimeChunk);
                } else {
                    request = buildRequest(dateField, fromDate, fromDate + adjustedTimeChunk);
                }
                reindexTask.setReindexParams("From: " + fromDate + " To: " + (fromDate + adjustedTimeChunk));
            } else {
                // For date string format, convert to formatted strings
                if (isRemote) {
                    request = buildRemoteRequest(source,
                            dateField,
                            GeneralUtils.convertTimestampToDateString(fromDate, dateFormat, isNanos),
                            GeneralUtils.convertTimestampToDateString(fromDate + adjustedTimeChunk, dateFormat, isNanos));
                } else {
                    request = buildRequest(dateField,
                            GeneralUtils.convertTimestampToDateString(fromDate, dateFormat, isNanos),
                            GeneralUtils.convertTimestampToDateString(fromDate + adjustedTimeChunk, dateFormat, isNanos));
                }
                reindexTask.setReindexParams("From: " + GeneralUtils.convertTimestampToDateString(fromDate, dateFormat, isNanos) +
                        " To: " + GeneralUtils.convertTimestampToDateString((fromDate + adjustedTimeChunk), dateFormat, isNanos));
            }

            reindexTask.setReindexRequest(request);
            // Query is automatically extracted and set by setReindexRequest()

            reindexTasks.add(reindexTask);
            fromDate += adjustedTimeChunk;
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
                                        final Object startValue,
                                        final Object endValue) {
        ReindexRequest request = new ReindexRequest();
        RangeQueryBuilder query;

        // RangeQueryBuilder can handle both numeric and string values
        if (startValue instanceof Long && endValue instanceof Long) {
            // For numeric values (epoch timestamps)
            query = new RangeQueryBuilder(dateField).gte((Long) startValue).lt((Long) endValue);
        } else {
            // For string values (formatted dates)
            query = new RangeQueryBuilder(dateField).gte(startValue.toString()).lt(endValue.toString());
        }
        request.setSourceQuery(query);
        return request;
    }

    private ReindexRequest buildRemoteRequest(final EsSettings source,
                                              final String dateField,
                                              final Object startValue,
                                              final Object endValue) {
        ReindexRequest request = new ReindexRequest();
        ESHostPOJO esHost = new ESHostPOJO(source);

        RangeQueryBuilder query;
        // RangeQueryBuilder can handle both numeric and string values
        if (startValue instanceof Long && endValue instanceof Long) {
            // For numeric values (epoch timestamps)
            query = new RangeQueryBuilder(dateField).gte((Long) startValue).lt((Long) endValue);
        } else {
            // For string values (formatted dates)
            query = new RangeQueryBuilder(dateField).gte(startValue.toString()).lt(endValue.toString());
        }

        request.setRemoteInfo(
                new RemoteInfo(
                        esHost.getProtocol(),
                        esHost.getDomain(),
                        esHost.getPort(),
                        null,
                        new BytesArray(query.toString()),
                        source.getUsername(), source.getPassword(), Collections.emptyMap(),
                        new TimeValue(100, TimeUnit.MILLISECONDS),
                        new TimeValue(100, TimeUnit.SECONDS)
                ));
        return request;
    }

    /**
     * Extracts the timestamp format (epoch_millis or epoch_nanos) from the date format string
     * Input: "yyyy-MM-dd'T'HH:mm:ss.SSSZZ (epoch_nanos)" → Output: "epoch_nanos"
     * Input: "epoch_nanos" → Output: "epoch_nanos"
     * Input: "yyyy-MM-dd" → Output: "epoch_millis" (default)
     */
    private String extractTimestampFormat(final String dateFormatString) {
        // Check if it's just the epoch type
        if (dateFormatString.equalsIgnoreCase("epoch_millis") ||
            dateFormatString.equalsIgnoreCase("epoch_nanos")) {
            return dateFormatString.toLowerCase();
        }

        // Extract from parentheses
        if (dateFormatString.contains("(") && dateFormatString.contains(")")) {
            int startIdx = dateFormatString.lastIndexOf("(");
            int endIdx = dateFormatString.lastIndexOf(")");
            if (startIdx < endIdx) {
                return dateFormatString.substring(startIdx + 1, endIdx).trim();
            }
        }
        return ETimestampFormat.EPOCH_MILLIS.getVarForUI();
    }

    /**
     * Extracts the SimpleDateFormat pattern from the date format string (removing timestamp type notation)
     * Input: "yyyy-MM-dd'T'HH:mm:ss.SSSZZ (epoch_millis)" → Output: "yyyy-MM-dd'T'HH:mm:ss.SSSZZ"
     * Input: "epoch_millis" → Output: "yyyy-MM-dd'T'HH:mm:ss.SSSZZ" (default pattern)
     * Input: "yyyy-MM-dd" → Output: "yyyy-MM-dd"
     */
    private String extractDatePattern(final String dateFormatString) {
        // If it's just the epoch type, return default pattern
        if (dateFormatString.equalsIgnoreCase("epoch_millis") ||
            dateFormatString.equalsIgnoreCase("epoch_nanos")) {
            return "yyyy-MM-dd'T'HH:mm:ss.SSSZZ";  // Default pattern
        }

        // Extract clean pattern from full format
        if (dateFormatString.contains("(") && dateFormatString.contains(")")) {
            int idx = dateFormatString.lastIndexOf("(");
            return dateFormatString.substring(0, idx).trim();
        }
        return dateFormatString;
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


