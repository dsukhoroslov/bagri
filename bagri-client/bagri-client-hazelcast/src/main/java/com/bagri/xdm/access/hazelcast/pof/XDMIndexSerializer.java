package com.bagri.xdm.access.hazelcast.pof;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.bagri.xdm.domain.XDMIndex;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.StreamSerializer;

public class XDMIndexSerializer implements StreamSerializer<XDMIndex> {

	@Override
	public void destroy() {
	}

	@Override
	public int getTypeId() {
		return XDMPortableFactory.cli_XDMIndex;
	}

	@Override
	public XDMIndex read(ObjectDataInput in) throws IOException {
		
		long[] ids = in.readLongArray();
		List<Long> docIds = new ArrayList<Long>(ids.length);
		for (long docId: ids) {
			docIds.add(docId);
		}
		return new XDMIndex(docIds);
	}

	@Override
	public void write(ObjectDataOutput out, XDMIndex xIndex) throws IOException {
		
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
