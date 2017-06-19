package com.bagri.xquery.saxon;

import static com.bagri.core.Constants.bg_schema;

import com.bagri.core.server.api.DocumentManagement;

import net.sf.saxon.lib.Resource;

public abstract class ResourceImplBase implements Resource {

	protected long docKey;
	protected DocumentManagement docMgr;
	
	public ResourceImplBase(DocumentManagement docMgr, long docKey) {
		this.docMgr = docMgr;
		this.docKey = docKey;
	}

	@Override
	public String getResourceURI() {
		return bg_schema + ":/" + docKey;
	}

	@Override
	public String getContentType() {
		return null;
	}
	
}
