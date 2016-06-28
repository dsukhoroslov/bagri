package com.bagri.xdm.client.hazelcast.data;

import static com.bagri.xdm.client.hazelcast.serialize.DataSerializationFactoryImpl.cli_QueryParamsKey;
import static com.bagri.xdm.client.hazelcast.serialize.DataSerializationFactoryImpl.factoryId;

import java.io.IOException;

import com.bagri.xdm.common.QueryResultKey;
import com.hazelcast.core.PartitionAware;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;

public class QueryParamsKey extends QueryResultKey implements IdentifiedDataSerializable { //, PartitionAware<Integer> { 

	public QueryParamsKey() {
		super();
	}

	public QueryParamsKey(int queryHash, long paramsHash) {
		super(queryHash, paramsHash);
	}

	//@Override
	//public Integer getPartitionKey() {
	//	return queryHash;
	//}

	@Override
	public int getFactoryId() {
		return factoryId;
	}

	@Override
	public int getId() {
		return cli_QueryParamsKey;
	}

	@Override
	public void readData(ObjectDataInput in) throws IOException {
		queryHash = in.readInt();
		paramsHash = in.readLong();
	}

	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		out.writeInt(queryHash);
		out.writeLong(paramsHash);
	}

}
