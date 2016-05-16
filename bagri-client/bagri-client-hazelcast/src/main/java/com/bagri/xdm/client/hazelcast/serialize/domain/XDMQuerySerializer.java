package com.bagri.xdm.client.hazelcast.serialize.domain;

import java.io.IOException;

import com.bagri.common.query.QueryBuilder;
import com.bagri.xdm.client.hazelcast.serialize.DataSerializationFactoryImpl;
import com.bagri.xdm.domain.XDMQuery;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.StreamSerializer;

public class XDMQuerySerializer implements StreamSerializer<XDMQuery> {

	@Override
	public int getTypeId() {
		return DataSerializationFactoryImpl.cli_XDMQuery;
	}

	@Override
	public void destroy() {
	}

	@Override
	public XDMQuery read(ObjectDataInput in) throws IOException {
		return new XDMQuery(in.readUTF(),
				in.readBoolean(),
				(QueryBuilder) in.readObject());
	}

	@Override
	public void write(ObjectDataOutput out, XDMQuery xQuery) throws IOException {
		out.writeUTF(xQuery.getQuery());
		out.writeBoolean(xQuery.isReadOnly());
		out.writeObject(xQuery.getXdmQuery());
	}

}
