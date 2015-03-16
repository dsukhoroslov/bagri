package com.bagri.xdm.client.hazelcast.task.query;

import static com.bagri.xdm.client.hazelcast.serialize.XDMDataSerializationFactory.factoryId;

import java.io.IOException;

import com.bagri.common.query.ExpressionBuilder;
import com.bagri.common.query.ExpressionContainer;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;

public abstract class ResultBuilder implements IdentifiedDataSerializable {
	
	protected ExpressionContainer exp; 
	protected long txId;

	public ResultBuilder() {
		//
	}
	
	public ResultBuilder(ExpressionContainer exp, long txId) {
		this.exp = exp;
		this.txId = txId;
	}

	@Override
	public int getFactoryId() {
		return factoryId;
	}

	@Override
	public void readData(ObjectDataInput in) throws IOException {
		exp = in.readObject();
		txId = in.readLong();
	}

	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		out.writeObject(exp);
		out.writeLong(txId);
	}

}
