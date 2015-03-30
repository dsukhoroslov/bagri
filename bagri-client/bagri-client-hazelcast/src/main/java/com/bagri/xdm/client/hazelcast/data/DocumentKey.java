package com.bagri.xdm.client.hazelcast.data;

import java.io.IOException;

import com.bagri.xdm.common.XDMDocumentKey;
import com.hazelcast.core.PartitionAware;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;

public class DocumentKey extends XDMDocumentKey implements DataSerializable, PartitionAware<Long> {
	
	public DocumentKey() {
		super();
	}

	public DocumentKey(long key) {
		super(key);
	}

	public DocumentKey(long docId, int version) {
		super(docId, version);
	}

	@Override
	public Long getPartitionKey() {
		return getDocumentId();
	}
	
	@Override
	public void readData(ObjectDataInput in) throws IOException {
		key = in.readLong();
	}

	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		out.writeLong(key);
	}

}
