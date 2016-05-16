package com.bagri.xdm.client.hazelcast.serialize.domain;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.bagri.xdm.client.hazelcast.serialize.DataSerializationFactoryImpl;
import com.bagri.xdm.domain.XDMDocumentType;
import com.bagri.xdm.domain.XDMNamespace;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.StreamSerializer;

public class XDMDocumentTypeSerializer implements StreamSerializer<XDMDocumentType> {

	@Override
	public int getTypeId() {
		return DataSerializationFactoryImpl.cli_XDMDocumentType;
	}

	@Override
	public void destroy() {
	}

	@Override
	public XDMDocumentType read(ObjectDataInput in) throws IOException {
		
		XDMDocumentType xType = new XDMDocumentType(in.readInt(), in.readUTF());
		xType.setNormalized(in.readBoolean());
		Collection<String> schemas = in.readObject();
		for (String schema: schemas) {
			xType.addSchema(schema);
		}
		return xType;
	}

	@Override
	public void write(ObjectDataOutput out, XDMDocumentType xType)	throws IOException {
		
		out.writeInt(xType.getTypeId());
		out.writeUTF(xType.getRootPath());
		out.writeBoolean(xType.isNormalized());
		out.writeObject(xType.getSchemas());
	}

}
