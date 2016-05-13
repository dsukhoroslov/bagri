package com.bagri.xdm.client.hazelcast.data;

import java.io.IOException;
import java.io.Serializable;

import com.bagri.xdm.common.XDMDataKey;
import com.bagri.xdm.common.XDMDocumentKey;
import com.hazelcast.core.PartitionAware;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;

import static com.bagri.xdm.client.hazelcast.serialize.DataSerializationFactoryImpl.*;

public class DocumentPathKey extends XDMDataKey implements IdentifiedDataSerializable, PartitionAware<Integer> { //, Serializable {

	/**
	 * 
	 */
	//private static final long serialVersionUID = -3417262044970162673L;

	public DocumentPathKey() {
		super();
	}

	public DocumentPathKey(long documentKey, int pathId) {
		super(documentKey, pathId);
	}

	@Override
	public Integer getPartitionKey() {
		return XDMDocumentKey.toHash(documentKey);
	}

	@Override
	public int getFactoryId() {
		return factoryId;
	}

	@Override
	public int getId() {
		return cli_DocumentPathKey;
	}

	@Override
	public void readData(ObjectDataInput in) throws IOException {
		documentKey = in.readLong();
		pathId = in.readInt();
	}

	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		out.writeLong(documentKey);
		out.writeInt(pathId);
	}

}
