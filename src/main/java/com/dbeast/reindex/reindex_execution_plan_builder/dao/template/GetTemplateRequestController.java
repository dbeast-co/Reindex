package com.dbeast.reindex.reindex_execution_plan_builder.dao.template;

import org.elasticsearch.client.indices.GetIndexTemplatesRequest;

public class GetTemplateRequestController {

    public GetIndexTemplatesRequest generateGetTemplateRequest(final String templateName) {
        return new GetIndexTemplatesRequest(templateName);
    }
}
