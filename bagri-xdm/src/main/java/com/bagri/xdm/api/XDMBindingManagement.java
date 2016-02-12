package com.bagri.xdm.api;

import com.bagri.xdm.common.XDMDocumentId;

public interface XDMBindingManagement {
	
	<T> T getDocumentBinding(XDMDocumentId docId) throws XDMException;
	<T> T getDocumentBinding(XDMDocumentId docId, Class<T> type) throws XDMException; 
	void setDocumentBinding(Object value) throws XDMException;
}
