package com.bagri.client.hazelcast.serialize;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import com.bagri.core.api.ContentSerializer;

public class StringContentSerializer implements ContentSerializer<String> {

	@Override
	public String readContent(DataInput in) throws IOException {
		return in.readUTF();
	}

	@Override
	public void writeContent(DataOutput out, String content) throws IOException {
		out.writeUTF(content);
	}

}
