package com.bagri.xdm.cache.store.mongo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.converter.Converter;



// move the key class to some common location!
import com.bagri.xdm.common.DataKey;
import com.bagri.xdm.common.KeyFactory;
import com.mongodb.DBObject;

public class DataDocumentKeyReadConverter implements Converter<DBObject, DataKey> {

    private static final Logger logger = LoggerFactory.getLogger(DataDocumentKeyReadConverter.class);
    
    private KeyFactory keyFactory;
    
    public void setKeyFactory(KeyFactory keyFactory) {
    	this.keyFactory = keyFactory;
    }

	@Override
	public DataKey convert(DBObject source) {
		logger.trace("convert.enter; source: {}", source); 
		return keyFactory.newDataKey((Long) source.get("document_id"), 
				(Integer) source.get("path_id"));
	}

}
