package com.dbeast.reindex.rest;

import com.dbeast.reindex.data_warehouse.DataWarehouse;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class ARest {
    protected static Logger logger = LogManager.getLogger();

    protected final ObjectMapper mapper = new ObjectMapper();
    protected final DataWarehouse dataWarehouse = DataWarehouse.getInstance();

    public abstract void rest();

    protected String objectToString(Object object) throws JsonProcessingException {
        mapper.configure(JsonGenerator.Feature.ESCAPE_NON_ASCII, false);
        return mapper.writeValueAsString(object);
    }
}
