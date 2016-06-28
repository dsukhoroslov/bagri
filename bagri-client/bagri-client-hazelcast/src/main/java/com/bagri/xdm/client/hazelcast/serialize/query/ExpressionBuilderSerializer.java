package com.bagri.xdm.client.hazelcast.serialize.query;

import java.io.IOException;
import java.util.List;

import com.bagri.xdm.client.hazelcast.serialize.DataSerializationFactoryImpl;
import com.bagri.xdm.query.Expression;
import com.bagri.xdm.query.ExpressionBuilder;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.StreamSerializer;

public class ExpressionBuilderSerializer implements StreamSerializer<ExpressionBuilder> {

	@Override
	public void destroy() {
	}

	@Override
	public int getTypeId() {
		return DataSerializationFactoryImpl.cli_ExpressionBuilder;
	}

	@Override
	public ExpressionBuilder read(ObjectDataInput in) throws IOException {
		ExpressionBuilder eb = new ExpressionBuilder();
		int size = in.readInt();
		for (int i=0; i < size; i++) {
			Expression ex = in.readObject();
			eb.addExpression(ex);
		}
		return eb;
	}

	@Override
	public void write(ObjectDataOutput out, ExpressionBuilder eb) throws IOException {
		List<Expression> expressions = eb.getExpressions();
		out.writeInt(expressions.size());
		for (Expression ex: expressions) {
			out.writeObject(ex);
		}
	}

}
