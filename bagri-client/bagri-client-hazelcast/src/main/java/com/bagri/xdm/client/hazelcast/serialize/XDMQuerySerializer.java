package com.bagri.xdm.client.hazelcast.serialize;

import java.io.IOException;

import com.bagri.common.query.QueryBuilder;
import com.bagri.xdm.domain.XDMQuery;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.StreamSerializer;

public class XDMQuerySerializer implements StreamSerializer<XDMQuery> {

	@Override
	public int getTypeId() {
		return XDMDataSerializationFactory.cli_XDMQuery;
	}

	@Override
	public void destroy() {
	}

	@Override
	public XDMQuery read(ObjectDataInput in) throws IOException {
		return new XDMQuery(in.readUTF(),
				null, //in.readObject(),
				(QueryBuilder) in.readObject());
	}

	@Override
	public void write(ObjectDataOutput out, XDMQuery xQuery) throws IOException {
		out.writeUTF(xQuery.getQuery());
		//out.writeObject(xQuery.getXqExpression());
		out.writeObject(xQuery.getXdmQuery());
	}

}
