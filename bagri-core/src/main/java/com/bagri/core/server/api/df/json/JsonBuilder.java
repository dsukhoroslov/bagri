package com.bagri.core.server.api.df.json;

import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

import com.bagri.core.DataKey;
import com.bagri.core.api.BagriException;
import com.bagri.core.model.Elements;
import com.bagri.core.server.api.ContentBuilder;

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
 	public void init(Properties properties) {
 		//
 		//logger.trace("init; got context: {}", context);
 	}
 
 	/**
	 * {@inheritDoc}
	 */
	@Override
	public String buildString(Map<DataKey, Elements> elements) throws BagriException {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public InputStream buildStream(Map<DataKey, Elements> elements) throws BagriException {
		return null;
	}

}
