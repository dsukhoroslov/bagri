package com.bagri.xdm.client.hazelcast.serialize;

import java.io.IOException;

import com.bagri.common.query.BinaryExpression;
import com.bagri.common.query.Comparison;
import com.bagri.common.query.Expression;
import com.bagri.common.query.PathBuilder;
import com.bagri.common.query.PathExpression;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.StreamSerializer;

public class ExpressionSerializer implements StreamSerializer<Expression> {

	@Override
	public void destroy() {
	}

	@Override
	public int getTypeId() {
		return XDMDataSerializationFactory.cli_Expression;
	}

	@Override
	public Expression read(ObjectDataInput in) throws IOException {
		int docType = in.readInt();
		String comp = in.readUTF();
		Comparison compType = Comparison.valueOf(comp);
		PathBuilder path = in.readObject();
		if (compType.isBinary(compType)) {
			return new BinaryExpression(docType, compType, path);
		} else {
			return new PathExpression(docType, compType, path, in.readUTF());
		}
	}

	@Override
	public void write(ObjectDataOutput out, Expression exp) throws IOException {
		out.writeInt(exp.getDocType());
		out.writeUTF(exp.getCompType().name());
		out.writeObject(exp.getPath());
		if (exp instanceof PathExpression) {
			out.writeUTF(((PathExpression) exp).getParamName());
		}
	}

}
