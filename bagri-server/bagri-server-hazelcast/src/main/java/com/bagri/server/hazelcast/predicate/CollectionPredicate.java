package com.bagri.server.hazelcast.predicate;

import static com.bagri.server.hazelcast.serialize.SystemSerializationFactory.cli_CollectionPredicate;
import static com.bagri.server.hazelcast.serialize.SystemSerializationFactory.cli_factory_id;

import java.io.IOException;
import java.util.Map.Entry;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

import com.bagri.core.DocumentKey;
import com.bagri.core.model.Document;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;
import com.hazelcast.query.Predicate;

public class CollectionPredicate implements Predicate<DocumentKey, Document>, IdentifiedDataSerializable {

	//private static final transient Logger logger = LoggerFactory.getLogger(CollectionPredicate.class);
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private int clnId;
	
	public CollectionPredicate() {
		// for de-serialization
		//logger.trace("<init>");
	}
	
	public CollectionPredicate(int clnId) {
		this.clnId = clnId;
		//logger.trace("<init>; clnId: {}", clnId);
	}

	@Override
	public int getFactoryId() {
		return cli_factory_id;
	}
	
	@Override
	public int getId() {
		return cli_CollectionPredicate;
	}

	@Override
	public boolean apply(Entry<DocumentKey, Document> docEntry) {
		Document doc = docEntry.getValue();
		// will perform this check later anyway..
		//logger.info("apply; doc: {}", doc); 
		return doc.hasCollection(clnId); // && doc.getTxFinish() == 0;
	}
	
	@Override
	public void readData(ObjectDataInput in) throws IOException {
		clnId = in.readInt();
		//logger.info("read; clnId: {}", clnId);
	}

	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		out.writeInt(clnId);
		//logger.info("write; clnId: {}", clnId);
	}

}
