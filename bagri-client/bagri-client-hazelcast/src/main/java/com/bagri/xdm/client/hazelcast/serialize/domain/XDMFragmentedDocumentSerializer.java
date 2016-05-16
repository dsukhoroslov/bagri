package com.bagri.xdm.client.hazelcast.serialize.domain;

import java.io.IOException;

import com.bagri.xdm.client.hazelcast.serialize.DataSerializationFactoryImpl;
import com.bagri.xdm.domain.XDMDocument;
import com.bagri.xdm.domain.XDMFragmentedDocument;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;

public class XDMFragmentedDocumentSerializer extends XDMDocumentSerializer {
	
	@Override
	public int getTypeId() {
		return DataSerializationFactoryImpl.cli_XDMFragmentedDocument;
	}

	@Override
	public XDMDocument read(ObjectDataInput in) throws IOException {
		
		XDMFragmentedDocument xDoc = new XDMFragmentedDocument(
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
	public void write(ObjectDataOutput out, XDMDocument xDoc) throws IOException {

		super.write(out, xDoc);
		out.writeLongArray(xDoc.getFragments());
	}
	

}


