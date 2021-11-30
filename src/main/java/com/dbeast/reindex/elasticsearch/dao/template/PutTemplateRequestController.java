package com.dbeast.reindex.elasticsearch.dao.template;

import org.elasticsearch.client.indices.PutIndexTemplateRequest;

public class PutTemplateRequestController {

    public PutIndexTemplateRequest generatePutTemplateRequest(final String templateName) {
        PutIndexTemplateRequest request = new PutIndexTemplateRequest("my-template");
        return request;
    }
}
