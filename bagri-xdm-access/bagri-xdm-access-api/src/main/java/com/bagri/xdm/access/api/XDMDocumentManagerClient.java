package com.bagri.xdm.access.api;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import com.bagri.common.query.ExpressionBuilder;
import com.bagri.xdm.XDMDocument;

public abstract class XDMDocumentManagerClient extends XDMDocumentManagerBase implements XDMDocumentManagement {
	

	protected abstract Long getDocumentId(String uri);
	
	@Override
	public XDMDocument getDocument(String uri) {
		return getDocument(getDocumentId(uri));
	}

	@Override
	public String getDocumentAsString(String uri) {
		return getDocumentAsString(getDocumentId(uri));
	}
	
	@Override
	public void removeDocument(String uri) {
		removeDocument(getDocumentId(uri));
	}
	
}
