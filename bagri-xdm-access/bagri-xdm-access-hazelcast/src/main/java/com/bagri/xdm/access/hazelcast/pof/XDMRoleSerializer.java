package com.bagri.xdm.access.hazelcast.pof;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import com.bagri.xdm.system.XDMPermission;
import com.bagri.xdm.system.XDMRole;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.StreamSerializer;

public class XDMRoleSerializer extends XDMEntitySerializer implements StreamSerializer<XDMRole> {

	@Override
	public int getTypeId() {
		return XDMPortableFactory.cli_XDMRole;
	}

	@Override
	public XDMRole read(ObjectDataInput in) throws IOException {
		Object[] entity = super.readEntity(in);
		XDMRole xRole = new XDMRole(
				(int) entity[0],
				(Date) entity[1],
				(String) entity[2],
				in.readUTF(), 
				in.readUTF(),
				(List<XDMPermission>) in.readObject(),
				(List<XDMRole>) in.readObject()); 
		return xRole;
	}

	@Override
	public void write(ObjectDataOutput out, XDMRole xRole)	throws IOException {
		super.writeEntity(out, xRole);
		out.writeUTF(xRole.getName());
		out.writeUTF(xRole.getDescription());
		// write permissions and roles
		out.writeObject(xRole.getPermissions());
		out.writeObject(xRole.getIncludedRoles());
	}



}
