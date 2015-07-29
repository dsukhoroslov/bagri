package com.bagri.xdm.client.hazelcast.serialize;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.bagri.xdm.domain.XDMUniqueDocument;
import com.bagri.xdm.domain.XDMUniqueValue;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.StreamSerializer;

public class XDMUniqueDocumentSerializer implements StreamSerializer<XDMUniqueDocument> {

	@Override
	public void destroy() {
	}

	@Override
	public int getTypeId() {
		return XDMDataSerializationFactory.cli_XDMUniqueDocument;
	}

	@Override
	public XDMUniqueDocument read(ObjectDataInput in) throws IOException {
		
		XDMUniqueDocument xIndex = new XDMUniqueDocument();
		int size = in.readInt();
		List<XDMUniqueValue> vals = new ArrayList<>(size);
		for (int i=0; i < size; i++) {
			XDMUniqueValue val = in.readObject();
			vals.add(val);
		}
		xIndex.setDocumentValues(vals);
		return xIndex;
	}

	@Override
	public void write(ObjectDataOutput out, XDMUniqueDocument xIndex) throws IOException {
		
		Collection<XDMUniqueValue> vals = xIndex.getDocumentValues();
		out.writeInt(vals.size());
		for (XDMUniqueValue val: vals) {
			out.writeObject(val);
		}
	}


}
