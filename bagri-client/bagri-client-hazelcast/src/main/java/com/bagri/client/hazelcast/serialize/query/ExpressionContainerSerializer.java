package com.bagri.client.hazelcast.serialize.query;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.bagri.client.hazelcast.serialize.DataSerializationFactoryImpl;
import com.bagri.core.query.ExpressionBuilder;
import com.bagri.core.query.ExpressionContainer;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.StreamSerializer;

public class ExpressionContainerSerializer implements StreamSerializer<ExpressionContainer> {

	@Override
	public void destroy() {
	}

	@Override
	public int getTypeId() {
		return DataSerializationFactoryImpl.cli_ExpressionContainer;
	}

	@Override
	public ExpressionContainer read(ObjectDataInput in) throws IOException {
		ExpressionBuilder eBuilder = in.readObject();
		int size = in.readInt();
		Map<String, Object> params = new HashMap<>(size);
		if (size > 0) {
			for (int i=0; i < size; i++) {
				params.put(in.readUTF(), in.readObject());
			}
		}
		return new ExpressionContainer(eBuilder, params);
	}

	@Override
	public void write(ObjectDataOutput out, ExpressionContainer exp) throws IOException {
		out.writeObject(exp.getBuilder());
		Map<String, Object> params = exp.getParams();
		out.writeInt(params.size());
		for (Map.Entry<String, Object> param: params.entrySet()) {
			out.writeUTF(param.getKey());
			out.writeObject(param.getValue());
		}
	}

}
