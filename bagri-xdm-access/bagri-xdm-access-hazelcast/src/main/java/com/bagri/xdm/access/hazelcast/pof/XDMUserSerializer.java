package com.bagri.xdm.access.hazelcast.pof;

import java.io.IOException;
import java.util.Date;

import com.bagri.xdm.XDMUser;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.StreamSerializer;

public class XDMUserSerializer implements StreamSerializer<XDMUser> {

	@Override
	public int getTypeId() {
		return XDMPortableFactory.cli_XDMUser;
	}

	@Override
	public void destroy() {
		// what should we do here?
	}

	@Override
	public XDMUser read(ObjectDataInput in) throws IOException {
		
		XDMUser xUser = new XDMUser(in.readUTF(), 
				in.readUTF(),
				in.readBoolean(),
				new Date(in.readLong()),
				in.readUTF()); 
		return xUser;
	}

	@Override
	public void write(ObjectDataOutput out, XDMUser xUser)	throws IOException {
		
		out.writeUTF(xUser.getLogin());
		out.writeUTF(xUser.getPassword());
		out.writeBoolean(xUser.isActive());
		out.writeLong(xUser.getCreatedAt().getTime());
		out.writeUTF(xUser.getCreatedBy());
	}

}
