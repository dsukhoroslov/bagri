package com.bagri.xdm.api;

public interface XDMBindingManagement {
	
	<T> T getDocumentBinding(String uri) throws XDMException;
	<T> T getDocumentBinding(String uri, Class<T> type) throws XDMException; 
	void setDocumentBinding(String uri, Object value) throws XDMException;
}
