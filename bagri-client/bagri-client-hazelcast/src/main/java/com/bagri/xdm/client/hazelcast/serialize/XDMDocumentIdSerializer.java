package com.bagri.xdm.client.hazelcast.serialize;

import java.io.IOException;

import com.bagri.xdm.common.XDMDocumentId;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.StreamSerializer;

public class XDMDocumentIdSerializer implements StreamSerializer<XDMDocumentId> {

	@Override
	public void destroy() {
	}

	@Override
	public int getTypeId() {
		return XDMDataSerializationFactory.cli_XDMDocumentId;
	}

	@Override
	public XDMDocumentId read(ObjectDataInput in) throws IOException {
		
		return new XDMDocumentId(in.readLong(),	in.readUTF());
	}

	@Override
	public void write(ObjectDataOutput out, XDMDocumentId xId) throws IOException {
		
		out.writeLong(xId.getDocumentKey());
		out.writeUTF(xId.getDocumentUri());
	}



}
