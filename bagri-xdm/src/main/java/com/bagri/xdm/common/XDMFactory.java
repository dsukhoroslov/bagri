package com.bagri.xdm.common;

import com.bagri.xdm.domain.XDMElement;

public interface XDMFactory {
	
	XDMDataKey newXDMDataKey(long dataId, long documentId);
	XDMElement newXDMData();

}
