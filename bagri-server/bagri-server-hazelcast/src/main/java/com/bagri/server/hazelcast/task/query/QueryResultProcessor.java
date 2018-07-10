package com.bagri.server.hazelcast.task.query;

import static com.bagri.client.hazelcast.serialize.SystemSerializationFactory.cli_factory_id;
import static com.bagri.server.hazelcast.serialize.SystemSerializationFactory.cli_QueryResultProcessor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.bagri.core.model.QueryResult;
import com.hazelcast.core.Offloadable;
import com.hazelcast.map.EntryBackupProcessor;
import com.hazelcast.map.EntryProcessor;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;

@SuppressWarnings("serial")
public class QueryResultProcessor implements EntryProcessor<Long, QueryResult>, Offloadable, IdentifiedDataSerializable  {
	
	private Map<String, Object> params;
	private Map<Long, String> docKeys;
	private List<Object> results;
	
	public QueryResultProcessor() {
		// de-ser
	}
	
	public QueryResultProcessor(Map<String, Object> params, Map<Long, String> docKeys, List<Object> results) {
		this.params = params;
		this.docKeys = docKeys;
		this.results = results;
	}

	@Override
	public EntryBackupProcessor<Long, QueryResult> getBackupProcessor() {
		return null;
	}

	@Override
	public int getFactoryId() {
		return cli_factory_id;
	}

	@Override
	public int getId() {
		return cli_QueryResultProcessor;
	}

	@Override
	public String getExecutorName() {
		return Offloadable.NO_OFFLOADING; // PN_XDM_SCHEMA_POOL;
	}

	@Override
	public Object process(Entry<Long, QueryResult> entry) {
		QueryResult qr = new QueryResult(params, docKeys, results);
		if (entry.getValue() != null) {
			qr.merge(entry.getValue());
		}
		entry.setValue(qr);
		return null;
	}

	@Override
	public void readData(ObjectDataInput in) throws IOException {
		int size = in.readInt();
		if (size > 0) {
			params = new HashMap<String, Object>(size);
			for (int i=0; i < size; i++) {
				params.put(in.readUTF(), in.readObject());
			}
		}
		size = in.readInt();
		docKeys = new HashMap<Long, String>(size);
		for (int i=0; i < size; i++) {
			docKeys.put(in.readLong(), in.readUTF());
		}
		size = in.readInt();
		results = new ArrayList<>(size);
		for (int i=0; i < size; i++) {
			results.add(in.readObject());
		}
	}

	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		if (params == null) {
			out.writeInt(0);
		} else {
			out.writeInt(params.size());
			for (Map.Entry<String, Object> e: params.entrySet()) {
				out.writeUTF(e.getKey());
				out.writeObject(e.getValue());
			}
		}
		if (docKeys == null) {
			out.writeInt(0);
		} else {
			out.writeInt(docKeys.size());
			for (Map.Entry<Long, String> e: docKeys.entrySet()) {
				out.writeLong(e.getKey());
				out.writeUTF(e.getValue());
			}
		}
		if (results == null) {
			out.writeInt(results.size());
		} else {
			out.writeInt(results.size());
			for (Object o: results) {
				out.writeObject(o);
			}
		}
	}

	@Override
	public String toString() {
		return "QueryResultProcessor [params=" + params + ", docKeys=" + docKeys + ", results=" + results + "]";
	}
	
}
