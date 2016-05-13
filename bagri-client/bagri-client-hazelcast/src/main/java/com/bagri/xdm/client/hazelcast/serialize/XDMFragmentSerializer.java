package com.bagri.xdm.client.hazelcast.serialize;

import java.io.IOException;
import java.util.Date;

import com.bagri.xdm.system.XDMFragment;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.StreamSerializer;

public class XDMFragmentSerializer extends XDMEntitySerializer implements StreamSerializer<XDMFragment> {

	@Override
	public int getTypeId() {
		return DataSerializationFactoryImpl.cli_XDMFragment;
	}

	@Override
	public XDMFragment read(ObjectDataInput in) throws IOException {
		Object[] entity = super.readEntity(in);
		XDMFragment xFragment = new XDMFragment(
				(int) entity[0],
				(Date) entity[1],
				(String) entity[2],
				in.readUTF(),
				in.readUTF(),
				in.readUTF(),
				in.readUTF(),
				in.readBoolean());
		return xFragment;
	}

	@Override
	public void write(ObjectDataOutput out, XDMFragment xFragment) throws IOException {
		super.writeEntity(out, xFragment);
		out.writeUTF(xFragment.getName());
		out.writeUTF(xFragment.getDocumentType());
		out.writeUTF(xFragment.getPath());
		out.writeUTF(xFragment.getDescription());
		out.writeBoolean(xFragment.isEnabled());
	}


}
