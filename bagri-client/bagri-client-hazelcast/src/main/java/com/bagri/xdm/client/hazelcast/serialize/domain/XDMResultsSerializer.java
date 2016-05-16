package com.bagri.xdm.client.hazelcast.serialize.domain;

import static com.bagri.common.util.CollectionUtils.*; 

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import com.bagri.common.util.CollectionUtils;
import com.bagri.xdm.client.hazelcast.serialize.DataSerializationFactoryImpl;
import com.bagri.xdm.domain.XDMResults;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.StreamSerializer;

public class XDMResultsSerializer implements StreamSerializer<XDMResults> {

	@Override
	public void destroy() {
	}

	@Override
	public int getTypeId() {
		return DataSerializationFactoryImpl.cli_XDMResults;
	}

	@Override
	public XDMResults read(ObjectDataInput in) throws IOException {
		int size = in.readInt();
		Map<QName, Object> params = null;
		if (size > 0) {
			params = new HashMap<QName, Object>(size);
			for (int i=0; i < size; i++) {
				params.put((QName) in.readObject(), in.readObject());
			}
		}
		List<Long> docIds = toLongList(in.readLongArray());
		size = in.readInt();
		List results = new ArrayList(size);
		for (int i=0; i < size; i++) {
			results.add(in.readObject());
		}
		return new XDMResults(params, docIds, results);
	}

	@Override
	public void write(ObjectDataOutput out, XDMResults xreslts) throws IOException {
		if (xreslts.getParams() == null) {
			out.writeInt(0);
		} else {
			out.writeInt(xreslts.getParams().size());
			for (Map.Entry e: xreslts.getParams().entrySet()) {
				out.writeObject(e.getKey());
				out.writeObject(e.getValue());
			}
		}
		out.writeLongArray(toLongArray(xreslts.getDocIds()));
		out.writeInt(xreslts.getResults().size());
		for (Object o: xreslts.getResults()) {
			out.writeObject(o);
		}
	}

}
