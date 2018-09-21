package com.bagri.client.hazelcast.serialize;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.bagri.core.api.ContentSerializer;
import com.bagri.support.pool.ContentDataPool;

public class StringMapContentSerializer implements ContentSerializer<Map<String, String>> {

	@Override
	public Map<String, String> readContent(DataInput in) throws IOException {
		int size = in.readInt();
		Map<String, String> content = new HashMap<>(size);
		ContentDataPool cdPool = ContentDataPool.getDataPool();
		for (int i=0; i < size; i++) {
			content.put(cdPool.intern(in.readUTF()), in.readUTF());
		}
		return content;
	}

	@Override
	public void writeContent(DataOutput out, Map<String, String> content) throws IOException {
		out.writeInt(content.size());
		for (Map.Entry<String, String> e: content.entrySet()) {
			out.writeUTF(e.getKey());
			out.writeUTF(e.getValue());
		}
	}

}
