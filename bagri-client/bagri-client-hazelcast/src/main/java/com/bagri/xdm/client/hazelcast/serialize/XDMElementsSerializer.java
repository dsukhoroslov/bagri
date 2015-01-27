package com.bagri.xdm.client.hazelcast.serialize;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.bagri.xdm.domain.XDMElement;
import com.bagri.xdm.domain.XDMElements;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.StreamSerializer;

public class XDMElementsSerializer implements StreamSerializer<XDMElements> {

	@Override
	public void destroy() {
	}

	@Override
	public int getTypeId() {
		return XDMPortableFactory.cli_XDMElements;
	}

	@Override
	public XDMElements read(ObjectDataInput in) throws IOException {
		
		int pathId = in.readInt();
		XDMElements xelts = new XDMElements(pathId, null);
		int size = in.readInt();
		//Map<Long, XDMElement> elts = new HashMap<Long, XDMElement>(size);
		//for (int i=0; i < size; i++) {
		//	elts.put(in.readLong(), (XDMElement) in.readObject());
		//}
		//XDMElements xelts = new XDMElements(elts);
		for (int i=0; i < size; i++) {
			xelts.addElement((XDMElement) in.readObject());
		}
		return xelts;
	}

	@Override
	public void write(ObjectDataOutput out, XDMElements xelts) throws IOException {
		
		Map<Long, XDMElement> elts = xelts.getElements();
		out.writeInt(xelts.getPathId());
		out.writeInt(elts.size());
		for (Map.Entry<Long, XDMElement> entry: elts.entrySet()) {
			//out.writeLong(entry.getKey());
			out.writeObject(entry.getValue());
		}
	}


}
