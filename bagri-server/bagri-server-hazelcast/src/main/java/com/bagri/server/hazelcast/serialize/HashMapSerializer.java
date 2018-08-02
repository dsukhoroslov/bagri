package com.bagri.server.hazelcast.serialize;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.StreamSerializer;

public class HashMapSerializer implements StreamSerializer<HashMap<String, Object>> {

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
		for (int i=0; i < size; i++) {
			map.put(in.readUTF(), in.readObject());
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


