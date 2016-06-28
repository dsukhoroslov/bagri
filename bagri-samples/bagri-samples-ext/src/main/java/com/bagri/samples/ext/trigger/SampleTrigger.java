package com.bagri.samples.ext.trigger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.xdm.api.XDMRepository;
import com.bagri.xdm.cache.api.XDMTrigger;
import com.bagri.xdm.domain.Document;

public class SampleTrigger implements XDMTrigger {

	private static final transient Logger logger = LoggerFactory.getLogger(SampleTrigger.class);
	
	public void beforeInsert(Document doc, XDMRepository repo) {
		logger.trace("beforeInsert; doc: {}; repo: {}", doc, repo);
	}

	public void afterInsert(Document doc, XDMRepository repo) {
		logger.trace("afterInsert; doc: {}; repo: {}", doc, repo);
	}

	public void beforeUpdate(Document doc, XDMRepository repo) {
		logger.trace("beforeUpdate; doc: {}; repo: {}", doc, repo);
	}

	public void afterUpdate(Document doc, XDMRepository repo) {
		logger.trace("afterUpdate; doc: {}; repo: {}", doc, repo);
	}

	public void beforeDelete(Document doc, XDMRepository repo) {
		logger.trace("beforeDelete; doc: {}; repo: {}", doc, repo);
	}

	public void afterDelete(Document doc, XDMRepository repo) {
		logger.trace("afterDelete; doc: {}; repo: {}", doc, repo);
	}
	
}
