package com.bagri.xdm.access.hazelcast.pof;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import com.bagri.xdm.system.XDMPermission;
import com.bagri.xdm.system.XDMRole;
import com.bagri.xdm.system.XDMUser;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.StreamSerializer;

public class XDMUserSerializer extends XDMEntitySerializer implements StreamSerializer<XDMUser> {

	@Override
	public int getTypeId() {
		return XDMPortableFactory.cli_XDMUser;
	}

	@Override
	public XDMUser read(ObjectDataInput in) throws IOException {
		Object[] entity = super.readEntity(in);
		XDMUser xUser = new XDMUser(
				(int) entity[0],
				(Date) entity[1],
				(String) entity[2],
				in.readUTF(), 
				in.readUTF(),
				in.readBoolean(),
				(List<XDMPermission>) in.readObject(),
				(List<XDMRole>) in.readObject()); 
		return xUser;
	}

	@Override
	public void write(ObjectDataOutput out, XDMUser xUser)	throws IOException {
		super.writeEntity(out, xUser);
		out.writeUTF(xUser.getLogin());
		out.writeUTF(xUser.getPassword());
		out.writeBoolean(xUser.isActive());
		// write grants and roles
		out.writeObject(xUser.getGrants());
		out.writeObject(xUser.getAssignedRoles());
	}

}
