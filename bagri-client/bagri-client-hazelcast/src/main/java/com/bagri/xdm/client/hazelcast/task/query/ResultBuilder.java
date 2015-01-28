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

	public ResultBuilder() {
		//
	}
	
	public ResultBuilder(ExpressionContainer exp) {
		this.exp = exp;
	}

	@Override
	public int getFactoryId() {
		return factoryId;
	}

	@Override
	public void readData(ObjectDataInput in) throws IOException {
		exp = in.readObject();
	}

	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		out.writeObject(exp);
	}

}
