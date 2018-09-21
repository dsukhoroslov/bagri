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

public class ByteMapContentSerializer implements ContentSerializer<Map<String, byte[]>> {

	@Override
	public Map<String, byte[]> readContent(DataInput in) throws IOException {
		int size = in.readInt();
		Map<String, byte[]> content = new HashMap<>(size);
		ContentDataPool cdPool = ContentDataPool.getDataPool();
		for (int i=0; i < size; i++) {
			content.put(cdPool.intern(in.readUTF()), ((ObjectDataInput) in).readByteArray());
		}
		return content;
	}

	@Override
	public void writeContent(DataOutput out, Map<String, byte[]> content) throws IOException {
		out.writeInt(content.size());
		for (Map.Entry<String, byte[]> e: content.entrySet()) {
			out.writeUTF(e.getKey());
			((ObjectDataOutput) out).writeByteArray(e.getValue());
		}
	}

}
