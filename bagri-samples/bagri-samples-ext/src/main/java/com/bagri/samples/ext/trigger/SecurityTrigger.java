package com.bagri.samples.ext.trigger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.xdm.api.XDMException;
import com.bagri.xdm.api.SchemaRepository;
import com.bagri.xdm.cache.api.DocumentTrigger;
import com.bagri.xdm.domain.Document;
import com.tpox_benchmark.security.Security;

public class SecurityTrigger implements DocumentTrigger {

	private static final transient Logger logger = LoggerFactory.getLogger(SecurityTrigger.class);
		
	public void beforeInsert(Document doc, SchemaRepository repo) {
		logger.trace("beforeInsert; doc: {}; repo: {}", doc, repo);
	}

	public void afterInsert(Document doc, SchemaRepository repo) {
		logger.info("afterInsert.enter; doc: {}; repo: {}", doc, repo);
		try {
			Security sec = repo.getBindingManagement().getDocumentBinding(doc.getUri(), Security.class);
			logger.info("afterInsert.exit; got security: {}/{}/{}", sec.getName(), sec.getSymbol(), sec.getId());
		} catch (XDMException ex) { 
			logger.info("afterInsert.error; got exception: {}", ex.getMessage());
		}
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
