package com.bagri.xdm.cache.hazelcast.store.mongo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.converter.Converter;

import com.bagri.xdm.access.hazelcast.data.DataDocumentKey;
import com.mongodb.DBObject;

public class DataDocumentKeyReadConverter implements Converter<DBObject, DataDocumentKey> {

    private static final Logger logger = LoggerFactory.getLogger(DataDocumentKeyReadConverter.class);

	@Override
	public DataDocumentKey convert(DBObject source) {
		logger.trace("convert.enter; source: {}", source); 
		return new DataDocumentKey((Long) source.get("data_id"), (Long) source.get("document_id"));
	}

}
