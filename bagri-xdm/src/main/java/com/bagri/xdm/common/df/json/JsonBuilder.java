package com.bagri.xdm.common.df.json;

import java.io.InputStream;
import java.util.Map;

import com.bagri.xdm.api.XDMException;
import com.bagri.xdm.cache.api.ContentBuilder;
import com.bagri.xdm.common.DataKey;
import com.bagri.xdm.domain.Elements;

/**
 * XDM Builder implementation for JSON format. Uses reference implementation (Glassfish) of json genarator.
 * 
 *  NOTE: not implemented yet!
 * 
 * @author Denis Sukhoroslov
 *
 */
public class JsonBuilder implements ContentBuilder {

	// TODO: implement it!
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String buildString(Map<DataKey, Elements> elements) throws XDMException {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public InputStream buildStream(Map<DataKey, Elements> elements) throws XDMException {
		return null;
	}

}
