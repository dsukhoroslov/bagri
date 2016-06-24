package com.bagri.xdm.client.hazelcast.task.query;

import java.io.IOException;

import com.bagri.xdm.client.hazelcast.task.TransactionAwareTask;
import com.bagri.xdm.common.query.ExpressionContainer;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;

public abstract class ResultBuilder extends TransactionAwareTask implements IdentifiedDataSerializable {
	
	protected ExpressionContainer exp; 

	public ResultBuilder() {
		super();
	}
	
	public ResultBuilder(String clientId, long txId, ExpressionContainer exp) {
		super(clientId, txId);
		this.exp = exp;
	}

	@Override
	public void readData(ObjectDataInput in) throws IOException {
		super.readData(in);
		exp = in.readObject();
	}

	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		super.writeData(out);
		out.writeObject(exp);
	}

}
