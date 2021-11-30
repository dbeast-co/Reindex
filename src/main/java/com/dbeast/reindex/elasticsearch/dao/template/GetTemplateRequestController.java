package com.dbeast.reindex.elasticsearch.dao.template;

import org.elasticsearch.client.indices.GetIndexTemplatesRequest;

public class GetTemplateRequestController {

    public GetIndexTemplatesRequest generateGetTemplateRequest(final String templateName) {
        return new GetIndexTemplatesRequest(templateName);
    }
}
