package com.bagri.client.hazelcast.serialize;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.bagri.core.api.ContentSerializer;
import com.bagri.support.pool.ContentDataPool;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;

public class ObjectMapContentSerializer implements ContentSerializer<Map<String, Object>> {

	@Override
	public Map<String, Object> readContent(DataInput in) throws IOException {
		int size = in.readInt();
		Map<String, Object> content = new HashMap<>(size);
		ContentDataPool cdPool = ContentDataPool.getDataPool();
		for (int i=0; i < size; i++) {
			content.put(cdPool.intern(in.readUTF()), ((ObjectDataInput) in).readObject());
		}
		return content;
	}

	@Override
	public void writeContent(DataOutput out, Map<String, Object> content) throws IOException {
		out.writeInt(content.size());
		for (Map.Entry<String, Object> e: content.entrySet()) {
			out.writeUTF(e.getKey());
			((ObjectDataOutput) out).writeObject(e.getValue());
		}
	}

}
