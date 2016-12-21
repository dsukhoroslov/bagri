package com.bagri.client.hazelcast.serialize.model;

import java.io.IOException;

import com.bagri.client.hazelcast.serialize.DataSerializationFactoryImpl;
import com.bagri.core.model.Namespace;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.StreamSerializer;

public class NamespaceSerializer implements StreamSerializer<Namespace> {

	@Override
	public int getTypeId() {
		return DataSerializationFactoryImpl.cli_XDMNamespace;
	}

	@Override
	public void destroy() {
	}

	@Override
	public Namespace read(ObjectDataInput in) throws IOException {
		
		return new Namespace(in.readUTF(), in.readUTF(), in.readUTF());
	}

	@Override
	public void write(ObjectDataOutput out, Namespace xNS) throws IOException {
		
		out.writeUTF(xNS.getUri());
		out.writeUTF(xNS.getPrefix());
		out.writeUTF(xNS.getLocation());
	}

}
