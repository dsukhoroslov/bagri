package com.bagri.xdm.client.hazelcast.data;

import static com.bagri.xdm.client.hazelcast.serialize.DataSerializationFactoryImpl.cli_PathIndexKey;
import static com.bagri.xdm.client.hazelcast.serialize.DataSerializationFactoryImpl.factoryId;

import java.io.IOException;

import com.bagri.xdm.common.XDMIndexKey;
import com.hazelcast.core.PartitionAware;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;

public class PathIndexKey extends XDMIndexKey implements IdentifiedDataSerializable { //, PartitionAware<Integer> { //Portable { 

	public PathIndexKey() {
		super();
	}

	public PathIndexKey(int pathId, Object value) {
		super(pathId, value);
	}

	//@Override
	//public Integer getPartitionKey() {
	//	return getPathId();
	//}

	@Override
	public int getFactoryId() {
		return factoryId;
	}

	@Override
	public int getId() {
		return cli_PathIndexKey;
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
