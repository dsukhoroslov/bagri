package com.bagri.client.hazelcast.serialize.model;

import java.io.IOException;

import com.bagri.client.hazelcast.serialize.DataSerializationFactoryImpl;
import com.bagri.core.model.Document;
import com.bagri.core.model.FragmentedDocument;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;

public class FragmentedDocumentSerializer extends DocumentSerializer {
	
	@Override
	public int getTypeId() {
		return DataSerializationFactoryImpl.cli_XDMFragmentedDocument;
	}

	@Override
	public Document read(ObjectDataInput in) throws IOException {
		
		FragmentedDocument xDoc = new FragmentedDocument(
				in.readLong(),
				in.readUTF(),
				in.readInt(),
				in.readLong(),
				in.readLong(),
				new java.util.Date(in.readLong()),
				in.readUTF(),
				in.readUTF(),
				in.readInt(),
				in.readInt());
		xDoc.setCollections(in.readIntArray());
		xDoc.setFragments(in.readLongArray());
		return xDoc;
	}

	@Override
	public void write(ObjectDataOutput out, Document xDoc) throws IOException {

		super.write(out, xDoc);
		out.writeLongArray(xDoc.getFragments());
	}
	

}


