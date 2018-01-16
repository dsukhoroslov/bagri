package com.bagri.client.hazelcast.serialize.model;

import java.io.IOException;

import com.bagri.client.hazelcast.serialize.DomainSerializationFactory;
import com.bagri.core.model.Data;
import com.bagri.core.model.Element;
import com.bagri.core.model.Path;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.StreamSerializer;

public class DataSerializer implements StreamSerializer<Data> {

	@Override
	public void destroy() {
	}

	@Override
	public int getTypeId() {
		return DomainSerializationFactory.cli_XDMData;
	}

	@Override
	public Data read(ObjectDataInput in) throws IOException {
		return new Data(
				(Path) in.readObject(),
				(Element) in.readObject());
	}

	@Override
	public void write(ObjectDataOutput out, Data xData) throws IOException {
		out.writeObject(xData.getDataPath());
		out.writeObject(xData.getElement());
	}


}
