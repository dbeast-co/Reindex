package com.dbeast.reindex.elasticsearch;

public class DataPeriodFromEs {
    private long dataStartDate;
    private long dataEndDate;

    public DataPeriodFromEs(final long dataStartDate,
                            final long dataEndDate) {
        this.dataStartDate = dataStartDate;
        this.dataEndDate = dataEndDate;
    }

    public long getDataStartDate() {
        return dataStartDate;
    }

    public void setDataStartDate(long dataStartDate) {
        this.dataStartDate = dataStartDate;
    }

    public long getDataEndDate() {
        return dataEndDate;
    }

    public void setDataEndDate(long dataEndDate) {
        this.dataEndDate = dataEndDate;
    }

    @Override
    public String toString() {
        return "DataPeriodFromEs{" +
                "dataStartDate=" + dataStartDate +
                ", dataEndDate=" + dataEndDate +
                '}';
    }

}