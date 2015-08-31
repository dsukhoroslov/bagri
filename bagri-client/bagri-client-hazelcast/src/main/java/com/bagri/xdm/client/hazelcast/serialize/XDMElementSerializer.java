package com.bagri.xdm.client.hazelcast.serialize;

import java.io.IOException;

import com.bagri.xdm.domain.XDMElement;
import com.bagri.xdm.domain.XDMNodeKind;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.StreamSerializer;

public class XDMElementSerializer implements StreamSerializer<XDMElement> {

	@Override
	public void destroy() {
	}

	@Override
	public int getTypeId() {
		return XDMDataSerializationFactory.cli_XDMElement;
	}

	@Override
	public XDMElement read(ObjectDataInput in) throws IOException {
		
		XDMElement xData = new XDMElement(
				in.readLong(),
				in.readLong(),
				in.readObject());
		return xData;
	}

	@Override
	public void write(ObjectDataOutput out, XDMElement xElt) throws IOException {
		
		out.writeLong(xElt.getElementId());
		out.writeLong(xElt.getParentId());
		out.writeObject(xElt.getValue());
	}

}
