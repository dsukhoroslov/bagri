package com.bagri.xdm.cache.hazelcast.predicate;

import static com.bagri.xdm.client.hazelcast.serialize.XDMDataSerializationFactory.cli_CollectionPredicate;
import static com.bagri.xdm.client.hazelcast.serialize.XDMDataSerializationFactory.factoryId;

import java.io.IOException;
import java.util.Map.Entry;

import com.bagri.xdm.common.XDMDocumentKey;
import com.bagri.xdm.domain.XDMDocument;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;
import com.hazelcast.query.Predicate;

public class CollectionPredicate implements Predicate<XDMDocumentKey, XDMDocument>, IdentifiedDataSerializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3363497246879761975L;
	
	private int clnId;
	
	public CollectionPredicate() {
		// for de-serialization
	}
	
	public CollectionPredicate(int clnId) {
		this.clnId = clnId;
	}

	@Override
	public int getFactoryId() {
		return factoryId;
	}
	
	@Override
	public int getId() {
		return cli_CollectionPredicate;
	}

	@Override
	public boolean apply(Entry<XDMDocumentKey, XDMDocument> docEntry) {
		XDMDocument doc = docEntry.getValue(); 
		return doc.hasCollection(clnId) && doc.getTxFinish() == 0;
	}
	
	@Override
	public void readData(ObjectDataInput in) throws IOException {
		clnId = in.readInt();
	}

	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		out.writeInt(clnId);
	}

}
