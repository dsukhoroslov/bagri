package com.bagri.xdm.access.hazelcast.pof;

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
		return XDMPortableFactory.cli_XDMElement;
	}

	@Override
	public XDMElement read(ObjectDataInput in) throws IOException {
		
		XDMElement xData = new XDMElement(
				in.readLong(),
				in.readLong(),
				in.readUTF());
		return xData;
	}

	@Override
	public void write(ObjectDataOutput out, XDMElement xData) throws IOException {
		
		out.writeLong(xData.getElementId());
		out.writeLong(xData.getParentId());
		out.writeUTF(xData.getValue());
	}

}
