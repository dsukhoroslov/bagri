package com.bagri.xdm.common;

public interface XDMKeyFactory {
	
	XDMDocumentKey newXDMDocumentKey(long documentKey);
	XDMDocumentKey newXDMDocumentKey(long documentId, int version);
	XDMDocumentKey newXDMDocumentKey(String documentUri, int revision, int version);
	XDMDataKey newXDMDataKey(long documentKey, int pathId);
	XDMIndexKey newXDMIndexKey(int pathId, Object value);
	//XDMElement newXDMData();

}
