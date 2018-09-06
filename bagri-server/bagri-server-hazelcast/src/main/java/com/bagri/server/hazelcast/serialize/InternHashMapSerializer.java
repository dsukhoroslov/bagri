package com.bagri.server.hazelcast.serialize;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.server.hazelcast.impl.ContentDataPool;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.StreamSerializer;

public class InternHashMapSerializer implements StreamSerializer<HashMap<String, Object>> {

	private static final transient Logger logger = LoggerFactory.getLogger(InternHashMapSerializer.class);

	@Override
	public void destroy() {
	}

	@Override
	public int getTypeId() {
		return 99; //DomainSerializationFactory.cli_XDMData;
	}

	@Override
	public HashMap<String, Object> read(ObjectDataInput in) throws IOException {
		int size = in.readInt();
		HashMap<String, Object> map = new HashMap<>(size);
		//ContentDataPool cdPool = ContentDataPool.getDataPool();
		for (int i=0; i < size; i++) {
			String key = in.readUTF();
			//key = cdPool.intern(key);
			key = key.intern();
			Object value = in.readObject();
			if (value instanceof String) {
				//value = cdPool.intern((String) value);
				value = ((String) value).intern();
			}
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


