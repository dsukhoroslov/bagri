package com.bagri.xdm.api;

public interface XDMBindingManagement {
	
	<T> T getDocumentBinding(long docId) throws XDMException;
	<T> T getDocumentBinding(long docId, Class<T> type) throws XDMException; 
	void setDocumentBinding(Object value) throws XDMException;
}
