package com.bagri.xdm.cache.store.mongo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.converter.Converter;



// move the key class to some common location!
import com.bagri.xdm.common.XDMDataKey;
import com.bagri.xdm.common.XDMKeyFactory;
import com.mongodb.DBObject;

public class DataDocumentKeyReadConverter implements Converter<DBObject, XDMDataKey> {

    private static final Logger logger = LoggerFactory.getLogger(DataDocumentKeyReadConverter.class);
    
    private XDMKeyFactory keyFactory;
    
    public void setKeyFactory(XDMKeyFactory keyFactory) {
    	this.keyFactory = keyFactory;
    }

	@Override
	public XDMDataKey convert(DBObject source) {
		logger.trace("convert.enter; source: {}", source); 
		return keyFactory.newXDMDataKey((Long) source.get("document_id"), 
				(Integer) source.get("path_id"));
	}

}
