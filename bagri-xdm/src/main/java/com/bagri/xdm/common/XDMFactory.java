package com.bagri.xdm.common;

import com.bagri.xdm.domain.XDMElement;

public interface XDMFactory {
	
	XDMDocumentKey newXDMDocumentKey(long documentKey);
	XDMDocumentKey newXDMDocumentKey(long documentId, int version);
	XDMDataKey newXDMDataKey(long documentKey, int pathId);
	XDMIndexKey newXDMIndexKey(int pathId, Object value);
	XDMElement newXDMData();

}
