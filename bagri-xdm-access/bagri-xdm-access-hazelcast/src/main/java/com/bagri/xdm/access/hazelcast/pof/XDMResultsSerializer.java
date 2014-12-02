package com.bagri.xdm.access.hazelcast.pof;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.bagri.xdm.domain.XDMResults;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.StreamSerializer;

public class XDMResultsSerializer  implements StreamSerializer<XDMResults> {

	@Override
	public void destroy() {
	}

	@Override
	public int getTypeId() {
		return XDMPortableFactory.cli_XDMResults;
	}

	@Override
	public XDMResults read(ObjectDataInput in) throws IOException {
		int size = in.readInt();
		Map<String, Object> params = new HashMap<String, Object>(size);
		for (int i=0; i < size; i++) {
			params.put(in.readUTF(), in.readObject());
		}
		size = in.readInt();
		List<Long> docIds = new ArrayList<Long>(size);
		for (int i=0; i < size; i++) {
			docIds.add(in.readLong());
		}
		size = in.readInt();
		List results = new ArrayList(size);
		for (int i=0; i < size; i++) {
			results.add(in.readObject());
		}
		return new XDMResults(params, docIds, results);
	}

	@Override
	public void write(ObjectDataOutput out, XDMResults xreslts) throws IOException {
		out.writeInt(xreslts.getParams().size());
		for (Map.Entry e: xreslts.getParams().entrySet()) {
			out.writeUTF(e.getKey().toString());
			out.writeObject(e.getValue());
		}
		out.writeInt(xreslts.getDocIds().size());
		for (Long docId: xreslts.getDocIds()) {
			out.writeLong(docId);
		}
		out.writeInt(xreslts.getResults().size());
		for (Object o: xreslts.getResults()) {
			out.writeObject(o);
		}
	}

}
