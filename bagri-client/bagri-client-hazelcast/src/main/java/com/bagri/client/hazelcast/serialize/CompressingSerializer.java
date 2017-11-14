package com.bagri.client.hazelcast.serialize;

import java.io.IOException;

import com.bagri.core.api.ContentSerializer;
import com.hazelcast.internal.serialization.InternalSerializationService;
import com.hazelcast.nio.IOUtil;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;

public class CompressingSerializer {

	public static Object readCompressedData(InternalSerializationService ss, ObjectDataInput in) throws IOException {
		ObjectDataInput odi = ss.createObjectDataInput(IOUtil.decompress(in.readByteArray()));
		return odi.readObject();
	}

	public static void writeCompressedData(InternalSerializationService ss, ObjectDataOutput out, Object data) throws IOException {
		ObjectDataOutput tmp = ss.createObjectDataOutput();
		tmp.writeObject(data);
		out.writeByteArray(IOUtil.compress(tmp.toByteArray()));
	}
	
	public static Object readCompressedContent(InternalSerializationService ss, ObjectDataInput in, ContentSerializer<Object> cs) throws IOException {
		ObjectDataInput odi = ss.createObjectDataInput(IOUtil.decompress(in.readByteArray()));
		return cs.readContent(odi);
	}

	public static void writeCompressedContent(InternalSerializationService ss, ObjectDataOutput out, ContentSerializer<Object> cs, Object data) throws IOException {
		ObjectDataOutput tmp = ss.createObjectDataOutput();
		cs.writeContent(tmp, data);
		out.writeByteArray(IOUtil.compress(tmp.toByteArray()));
	}
}
