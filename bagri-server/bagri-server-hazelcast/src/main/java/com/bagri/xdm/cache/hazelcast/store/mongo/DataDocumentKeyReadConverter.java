package com.bagri.xdm.cache.hazelcast.store.mongo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.converter.Converter;

import com.bagri.xdm.access.hazelcast.data.DocumentPathKey;
import com.mongodb.DBObject;

public class DataDocumentKeyReadConverter implements Converter<DBObject, DocumentPathKey> {

    private static final Logger logger = LoggerFactory.getLogger(DataDocumentKeyReadConverter.class);

	@Override
	public DocumentPathKey convert(DBObject source) {
		logger.trace("convert.enter; source: {}", source); 
		return new DocumentPathKey((Long) source.get("document_id"), (Integer) source.get("path_id"));
	}

}
