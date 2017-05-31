package com.bagri.server.hazelcast.predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.core.model.QueryResult;
import com.hazelcast.query.extractor.ValueCollector;
import com.hazelcast.query.extractor.ValueExtractor;

public class ResultsQueryExtractor extends ValueExtractor<QueryResult, Object> {

	private static final transient Logger logger = LoggerFactory.getLogger(ResultsQueryExtractor.class);
	
	@Override
	public void extract(QueryResult target, Object argument, ValueCollector collector) {
		for (Long docId: target.getDocIds()) {
			collector.addObject(docId);
		}
		//logger.debug("extract.exit; added {} values to {}", target.getDocIds().size(), collector);
	}

}
