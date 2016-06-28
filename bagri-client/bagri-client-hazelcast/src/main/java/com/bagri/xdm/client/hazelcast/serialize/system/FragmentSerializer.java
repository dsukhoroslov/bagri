package com.bagri.xdm.client.hazelcast.serialize.system;

import java.io.IOException;
import java.util.Date;

import com.bagri.xdm.client.hazelcast.serialize.DataSerializationFactoryImpl;
import com.bagri.xdm.system.Fragment;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.StreamSerializer;

public class FragmentSerializer extends EntitySerializer implements StreamSerializer<Fragment> {

	@Override
	public int getTypeId() {
		return DataSerializationFactoryImpl.cli_XDMFragment;
	}

	@Override
	public Fragment read(ObjectDataInput in) throws IOException {
		Object[] entity = super.readEntity(in);
		Fragment xFragment = new Fragment(
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
	public void write(ObjectDataOutput out, Fragment xFragment) throws IOException {
		super.writeEntity(out, xFragment);
		out.writeUTF(xFragment.getName());
		out.writeUTF(xFragment.getDocumentType());
		out.writeUTF(xFragment.getPath());
		out.writeUTF(xFragment.getDescription());
		out.writeBoolean(xFragment.isEnabled());
	}


}
