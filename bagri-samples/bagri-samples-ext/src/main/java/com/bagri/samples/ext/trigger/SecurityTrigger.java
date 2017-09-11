package com.bagri.samples.ext.trigger;

import static com.bagri.core.Constants.pn_document_data_format;

import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.core.api.SchemaRepository;
import com.bagri.core.api.BagriException;
import com.bagri.core.api.DocumentAccessor;
import com.bagri.core.model.Document;
import com.bagri.core.server.api.DocumentTrigger;
import com.tpox_benchmark.security.Security;

public class SecurityTrigger implements DocumentTrigger {

	private static final transient Logger logger = LoggerFactory.getLogger(SecurityTrigger.class);
		
	public void beforeInsert(Document doc, SchemaRepository repo) {
		logger.trace("beforeInsert; doc: {}; repo: {}", doc, repo);
	}

	public void afterInsert(Document doc, SchemaRepository repo) {
		logger.info("afterInsert.enter; doc: {}; repo: {}", doc, repo);
		try {
			Properties props = new Properties();
			props.setProperty(pn_document_data_format, "XML");
			DocumentAccessor da = repo.getDocumentManagement().getDocument(doc.getUri(), props);
			Security sec = da.getContent();
			logger.info("afterInsert.exit; got security: {}/{}/{}", sec.getName(), sec.getSymbol(), sec.getId());
		} catch (BagriException ex) { 
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
