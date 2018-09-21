package com.bagri.client.hazelcast.serialize;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.bagri.support.pool.ContentDataPool;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.StreamSerializer;

public class HashMapSerializer implements StreamSerializer<HashMap<String, Object>> {

	@Override
	public void destroy() {
	}

	@Override
	public int getTypeId() {
		return SystemSerializationFactory.cli_HashMap;
	}

	@Override
	public HashMap<String, Object> read(ObjectDataInput in) throws IOException {
		int size = in.readInt();
		HashMap<String, Object> map = new HashMap<>(size);
		ContentDataPool cdPool = ContentDataPool.getDataPool();
		for (int i=0; i < size; i++) {
			String key = in.readUTF();
			key = cdPool.intern(key);
			//key = key.intern();
			Object value = in.readObject();
			//if (value instanceof String) {
			//	value = cdPool.intern((String) value);
				//value = ((String) value).intern();
			//}
			map.put(key, value);
		}
		return map;
	}

	@Override
	public void write(ObjectDataOutput out, HashMap<String, Object> map) throws IOException {
		out.writeInt(map.size());
		for (Map.Entry<String, Object> e: map.entrySet()) {
			out.writeUTF(e.getKey());
			out.writeObject(e.getValue());
		}
	}

}


