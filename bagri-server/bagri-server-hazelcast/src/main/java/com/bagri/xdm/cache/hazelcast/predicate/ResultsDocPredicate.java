package com.bagri.xdm.cache.hazelcast.predicate;

import static com.bagri.xdm.client.hazelcast.serialize.DataSerializationFactoryImpl.factoryId;
import static com.bagri.xdm.client.hazelcast.serialize.DataSerializationFactoryImpl.cli_ResultsDocPredicate;

import java.io.IOException;
import java.util.Map.Entry;

import com.bagri.xdm.domain.XDMResults;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;
import com.hazelcast.query.Predicate;

public class ResultsDocPredicate implements Predicate<Long, XDMResults>, IdentifiedDataSerializable { 
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -8874751974148908253L;
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
		return factoryId;
	}
	
	@Override
	public int getId() {
		return cli_ResultsDocPredicate;
	}

	@Override
	public boolean apply(Entry<Long, XDMResults> resEntry) {
		return resEntry.getValue().getDocIds().contains(docId);
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
