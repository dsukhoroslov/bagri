package com.bagri.xdm.client.hazelcast.serialize;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.bagri.xdm.domain.XDMIndexedDocument;
import com.bagri.xdm.domain.XDMIndexedValue;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.StreamSerializer;

public class XDMIndexedDocumentSerializer implements StreamSerializer<XDMIndexedDocument> {

	@Override
	public void destroy() {
	}

	@Override
	public int getTypeId() {
		return XDMDataSerializationFactory.cli_XDMIndexedDocument;
	}

	@Override
	public XDMIndexedDocument read(ObjectDataInput in) throws IOException {
		
		int pathId = in.readInt();
		Object value = in.readObject();
		long[] ids = in.readLongArray();
		List<Long> docIds = new ArrayList<Long>(ids.length);
		for (long docId: ids) {
			docIds.add(docId);
		}
		return new XDMIndexedDocument(pathId, value, docIds);
	}

	@Override
	public void write(ObjectDataOutput out, XDMIndexedDocument xIndex) throws IOException {
		
		out.writeInt(xIndex.getPathId());
		out.writeObject(xIndex.getValue());
		Set<Long> docIds = xIndex.getDocumentIds();
		long[] ids = new long[docIds.size()];
		int idx = 0;
		for (Iterator<Long> itr = docIds.iterator(); itr.hasNext();) {
			ids[idx] = itr.next();
			idx++;
		}
		out.writeLongArray(ids);
	}


}
