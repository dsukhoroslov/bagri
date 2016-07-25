package com.bagri.xdm.client.hazelcast.serialize.domain;

import static com.bagri.common.util.CollectionUtils.*; 

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.bagri.common.util.CollectionUtils;
import com.bagri.xdm.client.hazelcast.serialize.DataSerializationFactoryImpl;
import com.bagri.xdm.domain.QueryResult;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.StreamSerializer;

public class QueryResultSerializer implements StreamSerializer<QueryResult> {

	@Override
	public void destroy() {
	}

	@Override
	public int getTypeId() {
		return DataSerializationFactoryImpl.cli_XDMResults;
	}

	@Override
	public QueryResult read(ObjectDataInput in) throws IOException {
		int size = in.readInt();
		Map<String, Object> params = null;
		if (size > 0) {
			params = new HashMap<String, Object>(size);
			for (int i=0; i < size; i++) {
				params.put(in.readUTF(), in.readObject());
			}
		}
		size = in.readInt();
		Map<Long, String> docKeys = new HashMap<Long, String>(size);
		for (int i=0; i < size; i++) {
			docKeys.put(in.readLong(), in.readUTF());
		}
		size = in.readInt();
		List<Object> results = new ArrayList<>(size);
		for (int i=0; i < size; i++) {
			results.add(in.readObject());
		}
		return new QueryResult(params, docKeys, results);
	}

	@Override
	public void write(ObjectDataOutput out, QueryResult xreslts) throws IOException {
		if (xreslts.getParams() == null) {
			out.writeInt(0);
		} else {
			out.writeInt(xreslts.getParams().size());
			for (Map.Entry<String, Object> e: xreslts.getParams().entrySet()) {
				out.writeUTF(e.getKey());
				out.writeObject(e.getValue());
			}
		}
		out.writeInt(xreslts.getDocKeys().size());
		for (Map.Entry<Long, String> e: xreslts.getDocKeys().entrySet()) {
			out.writeLong(e.getKey());
			out.writeUTF(e.getValue());
		}
		out.writeInt(xreslts.getResults().size());
		for (Object o: xreslts.getResults()) {
			out.writeObject(o);
		}
	}

}
