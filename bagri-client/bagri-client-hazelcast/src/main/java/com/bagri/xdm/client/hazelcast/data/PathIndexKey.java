package com.bagri.xdm.client.hazelcast.data;

import java.io.IOException;

import com.bagri.xdm.common.XDMIndexKey;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;

public class PathIndexKey extends XDMIndexKey implements DataSerializable { //Portable, PartitionAware<Long> { 

	public PathIndexKey() {
		super();
	}

	public PathIndexKey(int pathId, Object value) {
		super(pathId, value);
	}

	@Override
	public void readData(ObjectDataInput in) throws IOException {
		pathId = in.readInt();
		value = in.readObject();
	}

	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		out.writeInt(pathId);
		out.writeObject(value);
	}

}
