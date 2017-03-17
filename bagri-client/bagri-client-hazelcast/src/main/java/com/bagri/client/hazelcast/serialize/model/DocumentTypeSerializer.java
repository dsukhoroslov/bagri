package com.bagri.client.hazelcast.serialize.model;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.bagri.client.hazelcast.serialize.DataSerializationFactoryImpl;
import com.bagri.core.model.DocumentType;
import com.bagri.core.model.Namespace;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.StreamSerializer;

public class DocumentTypeSerializer implements StreamSerializer<DocumentType> {

	@Override
	public int getTypeId() {
		return DataSerializationFactoryImpl.cli_XDMDocumentType;
	}

	@Override
	public void destroy() {
	}

	@Override
	public DocumentType read(ObjectDataInput in) throws IOException {
		
		DocumentType xType = new DocumentType(in.readInt(), in.readUTF());
		//xType.setNormalized(in.readBoolean());
		Collection<String> schemas = in.readObject();
		for (String schema: schemas) {
			xType.addSchema(schema);
		}
		return xType;
	}

	@Override
	public void write(ObjectDataOutput out, DocumentType xType)	throws IOException {
		
		out.writeInt(xType.getTypeId());
		out.writeUTF(xType.getRootPath());
		//out.writeBoolean(xType.isNormalized());
		out.writeObject(xType.getSchemas());
	}

}
