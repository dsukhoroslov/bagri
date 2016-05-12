package com.bagri.xdm.cache.api;

import com.bagri.xdm.api.XDMException;

public interface XDMDocumentManagement extends com.bagri.xdm.api.XDMDocumentManagement {

	String getDocumentAsString(long docKey) throws XDMException;
	
}
