package com.bagri.xdm.cache.store.mongo;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.converter.Converter;

import com.bagri.xdm.common.XDMDataKey;

public class DataDocumentKeyWriteConverter implements Converter<XDMDataKey, DBObject> {

    private static final Logger logger = LoggerFactory.getLogger(DataDocumentKeyWriteConverter.class);

	@Override
	public DBObject convert(XDMDataKey source) {
		logger.trace("convert.enter; source: {}", source); 
	    DBObject dbo = new BasicDBObject();
	    //dbo.put("_id", source.getId());
	    dbo.put("document_key", source.getDocumentKey());
	    dbo.put("path_id", source.getPathId());
	    return dbo;	
	}

}
