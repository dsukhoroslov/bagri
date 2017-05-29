package com.bagri.server.hazelcast.predicate;

import com.bagri.core.model.QueryResult;
import com.hazelcast.query.extractor.ValueCollector;
import com.hazelcast.query.extractor.ValueExtractor;

public class ResultsQueryExtractor extends ValueExtractor<QueryResult, Object> {

	@Override
	public void extract(QueryResult target, Object argument, ValueCollector collector) {
		for (Long docId: target.getDocIds()) {
			collector.addObject(docId);
		}
	}

}
