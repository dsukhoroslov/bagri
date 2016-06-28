package com.bagri.xdm.client.hazelcast.serialize.domain;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.bagri.xdm.client.hazelcast.serialize.DataSerializationFactoryImpl;
import com.bagri.xdm.domain.UniqueDocument;
import com.bagri.xdm.domain.UniqueValue;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.StreamSerializer;

public class UniqueDocumentSerializer implements StreamSerializer<UniqueDocument> {

	@Override
	public void destroy() {
	}

	@Override
	public int getTypeId() {
		return DataSerializationFactoryImpl.cli_XDMUniqueDocument;
	}

	@Override
	public UniqueDocument read(ObjectDataInput in) throws IOException {
		
		UniqueDocument xIndex = new UniqueDocument();
		int size = in.readInt();
		List<UniqueValue> vals = new ArrayList<>(size);
		for (int i=0; i < size; i++) {
			UniqueValue val = in.readObject();
			vals.add(val);
		}
		xIndex.setDocumentValues(vals);
		return xIndex;
	}

	@Override
	public void write(ObjectDataOutput out, UniqueDocument xIndex) throws IOException {
		
		Collection<UniqueValue> vals = xIndex.getDocumentValues();
		out.writeInt(vals.size());
		for (UniqueValue val: vals) {
			out.writeObject(val);
		}
	}


}
