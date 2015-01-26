package com.bagri.xdm.access.hazelcast.pof;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.bagri.xdm.domain.XDMIndexedValue;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.StreamSerializer;

public class XDMIndexedValueSerializer implements StreamSerializer<XDMIndexedValue> {

	@Override
	public void destroy() {
	}

	@Override
	public int getTypeId() {
		return XDMPortableFactory.cli_XDMIndexedValue;
	}

	@Override
	public XDMIndexedValue read(ObjectDataInput in) throws IOException {
		
		long[] ids = in.readLongArray();
		List<Long> docIds = new ArrayList<Long>(ids.length);
		for (long docId: ids) {
			docIds.add(docId);
		}
		return new XDMIndexedValue(docIds);
	}

	@Override
	public void write(ObjectDataOutput out, XDMIndexedValue xIndex) throws IOException {
		
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
