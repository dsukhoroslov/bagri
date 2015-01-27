package com.bagri.xdm.client.hazelcast.serialize;

import java.io.IOException;

import com.bagri.xdm.domain.XDMDocument;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.StreamSerializer;

public class XDMDocumentSerializer implements StreamSerializer<XDMDocument> {

	@Override
	public void destroy() {
	}

	@Override
	public int getTypeId() {
		return XDMPortableFactory.cli_XDMDocument;
	}

	@Override
	public XDMDocument read(ObjectDataInput in) throws IOException {
		
		return new XDMDocument(
				in.readLong(),
				in.readUTF(),
				in.readInt(),
				in.readInt(),
				new java.util.Date(in.readLong()),
				in.readUTF(),
				in.readUTF());
	}

	@Override
	public void write(ObjectDataOutput out, XDMDocument xDoc) throws IOException {
		
		out.writeLong(xDoc.getDocumentId());
		out.writeUTF(xDoc.getUri());
		out.writeInt(xDoc.getTypeId());
		out.writeInt(xDoc.getVersion());
		out.writeLong(xDoc.getCreatedAt().getTime());
		out.writeUTF(xDoc.getCreatedBy());
		out.writeUTF(xDoc.getEncoding());
	}
	
}
