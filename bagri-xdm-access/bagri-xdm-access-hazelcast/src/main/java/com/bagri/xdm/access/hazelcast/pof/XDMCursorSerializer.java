package com.bagri.xdm.access.hazelcast.pof;

import java.io.IOException;

import com.bagri.xdm.access.hazelcast.impl.BagriXQCursor;
import com.bagri.xquery.api.XQCursor;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.StreamSerializer;

public class XDMCursorSerializer implements StreamSerializer<XQCursor> {

	@Override
	public int getTypeId() {
		return XDMPortableFactory.cli_XQCursor;
	}

	@Override
	public void destroy() {
		// do nothing here ?
	}

	@Override
	public XQCursor read(ObjectDataInput in) throws IOException {
		String qName = in.readUTF();
		return new BagriXQCursor(qName);
	}

	@Override
	public void write(ObjectDataOutput out, XQCursor cursor) throws IOException {
		
		out.writeUTF(((BagriXQCursor) cursor).getQueueName());
	}

}
