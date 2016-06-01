package com.bagri.xdm.common.df.json;

import java.io.InputStream;
import java.util.Map;

import com.bagri.xdm.api.XDMException;
import com.bagri.xdm.common.XDMBuilder;
import com.bagri.xdm.common.XDMDataKey;
import com.bagri.xdm.domain.XDMElements;

public class JsonBuilder implements XDMBuilder {

	// TODO: implement it!
	
	@Override
	public String buildString(Map<XDMDataKey, XDMElements> elements) throws XDMException {
		return null;
	}

	@Override
	public InputStream buildStream(Map<XDMDataKey, XDMElements> elements) throws XDMException {
		return null;
	}

}
