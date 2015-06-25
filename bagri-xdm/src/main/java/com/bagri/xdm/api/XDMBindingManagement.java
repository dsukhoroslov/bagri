package com.bagri.xdm.api;

public interface XDMBindingManagement {
	
	<T> T getDocumentBinding(long docId);
	<T> T getDocumentBinding(long docId, T type);
	Object getDocumentBinding(long docId, Class type);

}
