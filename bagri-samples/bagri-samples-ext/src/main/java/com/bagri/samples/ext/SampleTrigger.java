package com.bagri.samples.ext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.xdm.api.XDMRepository;
import com.bagri.xdm.domain.XDMDocument;
import com.bagri.xdm.domain.XDMTrigger;

public class SampleTrigger implements XDMTrigger {

	private static final transient Logger logger = LoggerFactory.getLogger(SampleTrigger.class);
	
	public boolean isSynchronous() {
		return true; //false;
	}
	
	public void beforeInsert(XDMDocument doc, XDMRepository repo) {
		logger.trace("beforeInsert; doc: {}; repo: {}", doc, repo);
	}

	public void afterInsert(XDMDocument doc, XDMRepository repo) {
		logger.trace("afterInsert; doc: {}; repo: {}", doc, repo);
	}

	public void beforeUpdate(XDMDocument doc, XDMRepository repo) {
		logger.trace("beforeUpdate; doc: {}; repo: {}", doc, repo);
	}

	public void afterUpdate(XDMDocument doc, XDMRepository repo) {
		logger.trace("afterUpdate; doc: {}; repo: {}", doc, repo);
	}

	public void beforeDelete(XDMDocument doc, XDMRepository repo) {
		logger.trace("beforeDelete; doc: {}; repo: {}", doc, repo);
	}

	public void afterDelete(XDMDocument doc, XDMRepository repo) {
		logger.trace("afterDelete; doc: {}; repo: {}", doc, repo);
	}
	
}
