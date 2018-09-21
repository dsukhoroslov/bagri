package com.bagri.client.hazelcast.serialize.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.bagri.client.hazelcast.serialize.DomainSerializationFactory;
import com.bagri.core.model.QueryResult;
import com.bagri.support.pool.ContentDataPool;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.StreamSerializer;

public class QueryResultSerializer implements StreamSerializer<QueryResult> {

	@Override
	public void destroy() {
	}

	@Override
	public int getTypeId() {
		return DomainSerializationFactory.cli_XDMResults;
	}

	@Override
	public QueryResult read(ObjectDataInput in) throws IOException {
		int size = in.readInt();
		Map<String, Object> params = null;
		ContentDataPool cdPool = ContentDataPool.getDataPool();
		if (size > 0) {
			params = new HashMap<>(size);
			for (int i=0; i < size; i++) {
				String key = in.readUTF();
				key = cdPool.intern(key);
				Object value = in.readObject();
				//if (value instanceof String) {
				//	value = cdPool.intern((String) value);
				//}
				params.put(key, value);
			}
		}
		size = in.readInt();
		Map<Long, String> docKeys = new HashMap<>(size);
		for (int i=0; i < size; i++) {
			docKeys.put(in.readLong(), cdPool.intern(in.readUTF()));
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
