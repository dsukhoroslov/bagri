package com.bagri.client.hazelcast.serialize;

import java.io.IOException;

import com.bagri.core.api.ContentSerializer;
import com.hazelcast.internal.serialization.InternalSerializationService;
import com.hazelcast.nio.IOUtil;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;

public class CompressingSerializer {

	public static Object readCompressedData(ObjectDataInput in) throws IOException {
		InternalSerializationService ss = in.getSerializationService();
		ObjectDataInput odi = ss.createObjectDataInput(IOUtil.decompress(in.readByteArray()));
		return odi.readObject();
	}

	public static void writeCompressedData(ObjectDataOutput out, Object data) throws IOException {
		InternalSerializationService ss = out.getSerializationService();
		ObjectDataOutput tmp = ss.createObjectDataOutput();
		tmp.writeObject(data);
		out.writeByteArray(IOUtil.compress(tmp.toByteArray()));
	}
	
	public static Object readCompressedContent(ObjectDataInput in, ContentSerializer<Object> cs) throws IOException {
		InternalSerializationService ss = in.getSerializationService();
		ObjectDataInput odi = ss.createObjectDataInput(IOUtil.decompress(in.readByteArray()));
		return cs.readContent(odi);
	}

	public static void writeCompressedContent(ObjectDataOutput out, ContentSerializer<Object> cs, Object data) throws IOException {
		InternalSerializationService ss = out.getSerializationService();
		ObjectDataOutput tmp = ss.createObjectDataOutput();
		cs.writeContent(tmp, data);
		out.writeByteArray(IOUtil.compress(tmp.toByteArray()));
	}
}
