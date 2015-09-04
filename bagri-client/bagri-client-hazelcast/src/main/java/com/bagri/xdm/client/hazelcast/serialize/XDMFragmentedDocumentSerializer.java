package com.bagri.xdm.client.hazelcast.serialize;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import com.bagri.xdm.domain.XDMDocument;
import com.bagri.xdm.domain.XDMFragmentedDocument;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;

public class XDMFragmentedDocumentSerializer extends XDMDocumentSerializer {
	
	@Override
	public int getTypeId() {
		return XDMDataSerializationFactory.cli_XDMFragmentedDocument;
	}

	@Override
	public XDMDocument read(ObjectDataInput in) throws IOException {
		
		XDMFragmentedDocument xDoc = new XDMFragmentedDocument(
				in.readLong(),
				in.readInt(),
				in.readUTF(),
				in.readInt(),
				in.readLong(),
				in.readLong(),
				new java.util.Date(in.readLong()),
				in.readUTF(),
				in.readUTF());
		xDoc.setFragments(in.readLongArray());
		return xDoc;
	}

	@Override
	public void write(ObjectDataOutput out, XDMDocument xDoc) throws IOException {

		super.write(out, xDoc);
		out.writeLongArray(xDoc.getFragments());
	}
	

}
