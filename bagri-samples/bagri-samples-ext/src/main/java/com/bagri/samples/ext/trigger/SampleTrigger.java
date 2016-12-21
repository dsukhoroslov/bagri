package com.bagri.samples.ext.trigger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.core.api.SchemaRepository;
import com.bagri.core.model.Document;
import com.bagri.core.server.api.DocumentTrigger;

public class SampleTrigger implements DocumentTrigger {

	private static final transient Logger logger = LoggerFactory.getLogger(SampleTrigger.class);
	
	public void beforeInsert(Document doc, SchemaRepository repo) {
		logger.trace("beforeInsert; doc: {}; repo: {}", doc, repo);
	}

	public void afterInsert(Document doc, SchemaRepository repo) {
		logger.trace("afterInsert; doc: {}; repo: {}", doc, repo);
	}

	public void beforeUpdate(Document doc, SchemaRepository repo) {
		logger.trace("beforeUpdate; doc: {}; repo: {}", doc, repo);
	}

	public void afterUpdate(Document doc, SchemaRepository repo) {
		logger.trace("afterUpdate; doc: {}; repo: {}", doc, repo);
	}

	public void beforeDelete(Document doc, SchemaRepository repo) {
		logger.trace("beforeDelete; doc: {}; repo: {}", doc, repo);
	}

	public void afterDelete(Document doc, SchemaRepository repo) {
		logger.trace("afterDelete; doc: {}; repo: {}", doc, repo);
	}
	
}
