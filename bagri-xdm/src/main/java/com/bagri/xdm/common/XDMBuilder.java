package com.bagri.xdm.common;

import java.io.InputStream;
import java.util.Map;

import com.bagri.xdm.api.XDMException;
import com.bagri.xdm.domain.XDMElements;

public interface XDMBuilder {

    String buildString(Map<XDMDataKey, XDMElements> elements) throws XDMException; 
    InputStream buildStream(Map<XDMDataKey, XDMElements> elements) throws XDMException; 
	
}
