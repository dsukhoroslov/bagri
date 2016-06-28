package com.bagri.xdm.client.hazelcast.serialize.domain;

import java.io.IOException;

import com.bagri.xdm.client.hazelcast.serialize.DataSerializationFactoryImpl;
import com.bagri.xdm.domain.Element;
import com.bagri.xdm.domain.NodeKind;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.StreamSerializer;

public class ElementSerializer implements StreamSerializer<Element> {

	@Override
	public void destroy() {
	}

	@Override
	public int getTypeId() {
		return DataSerializationFactoryImpl.cli_XDMElement;
	}

	@Override
	public Element read(ObjectDataInput in) throws IOException {
		
		Element xData = new Element(
				in.readInt(),
				in.readInt(),
				in.readObject());
		return xData;
	}

	@Override
	public void write(ObjectDataOutput out, Element xElt) throws IOException {
		
		out.writeInt(xElt.getElementId());
		out.writeInt(xElt.getParentId());
		out.writeObject(xElt.getValue());
	}

}
