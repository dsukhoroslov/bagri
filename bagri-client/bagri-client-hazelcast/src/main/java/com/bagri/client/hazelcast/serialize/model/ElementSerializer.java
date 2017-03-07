package com.bagri.client.hazelcast.serialize.model;

import java.io.IOException;

import com.bagri.client.hazelcast.serialize.DataSerializationFactoryImpl;
import com.bagri.core.model.Element;
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
				in.readIntArray(),
				in.readObject());
		return xData;
	}

	@Override
	public void write(ObjectDataOutput out, Element xElt) throws IOException {
		
		out.writeIntArray(xElt.getPosition());
		out.writeObject(xElt.getValue());
	}

}
