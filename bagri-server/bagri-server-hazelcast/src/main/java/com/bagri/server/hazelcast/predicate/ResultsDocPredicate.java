package com.bagri.server.hazelcast.predicate;

import static com.bagri.server.hazelcast.serialize.SystemSerializationFactory.cli_ResultsDocPredicate;
import static com.bagri.server.hazelcast.serialize.SystemSerializationFactory.cli_factory_id;

import java.io.IOException;
import java.util.Map.Entry;
import java.util.Set;

import com.bagri.core.model.QueryResult;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;
import com.hazelcast.query.IndexAwarePredicate;
import com.hazelcast.query.Predicate;
import com.hazelcast.query.impl.Index;
import com.hazelcast.query.impl.QueryContext;

public class ResultsDocPredicate implements IndexAwarePredicate<Long, QueryResult>, IdentifiedDataSerializable { //Predicate<Long, QueryResult>  
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	//private static final transient Logger logger = LoggerFactory.getLogger(ResultsDocPredicate.class);
	
	private Long docId;
	
	public ResultsDocPredicate() {
		// for de-serialization
	}
	
	public ResultsDocPredicate(Long docId) {
		this.docId = docId;
	}

	@Override
	public int getFactoryId() {
		return cli_factory_id;
	}
	
	@Override
	public int getId() {
		return cli_ResultsDocPredicate;
	}

	@Override
	public boolean apply(Entry<Long, QueryResult> resEntry) {
		return resEntry.getValue().getDocIds().contains(docId);
	}
	
	@Override
	public Set filter(QueryContext queryContext) {
		//Index idx = queryContext.getIndex("docId");
		return null; //idx.getRecords(docId);
	}

	@Override
	public boolean isIndexed(QueryContext queryContext) {
		return false; //true;
	}

	@Override
	public void readData(ObjectDataInput in) throws IOException {
		docId = in.readLong();
	}

	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		out.writeLong(docId);
	}

}
