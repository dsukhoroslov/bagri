package com.bagri.xdm.common;

import com.bagri.xdm.XDMElement;

public interface XDMFactory {
	
	XDMDataKey newXDMDataKey(long dataId, long documentId);
	XDMElement newXDMData();

}
