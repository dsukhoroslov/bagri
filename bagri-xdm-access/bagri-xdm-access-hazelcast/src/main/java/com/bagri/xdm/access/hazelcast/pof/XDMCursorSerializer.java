package com.bagri.xdm.access.hazelcast.pof;

import java.io.IOException;

import com.bagri.xdm.access.hazelcast.impl.HazelcastXQCursor;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.StreamSerializer;

public class XDMCursorSerializer implements StreamSerializer<HazelcastXQCursor> {

	@Override
	public int getTypeId() {
		return XDMPortableFactory.cli_XQCursor;
	}

	@Override
	public void destroy() {
		// do nothing here ?
	}

	@Override
	public HazelcastXQCursor read(ObjectDataInput in) throws IOException {
		String qName = in.readUTF();
		return new HazelcastXQCursor(qName);
	}

	@Override
	public void write(ObjectDataOutput out, HazelcastXQCursor cursor) throws IOException {
		
		out.writeUTF(((HazelcastXQCursor) cursor).getQueueName());
	}

}
