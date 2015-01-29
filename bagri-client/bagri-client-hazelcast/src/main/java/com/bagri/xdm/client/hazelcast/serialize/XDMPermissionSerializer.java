package com.bagri.xdm.client.hazelcast.serialize;

import java.io.IOException;
import java.util.Set;

import com.bagri.xdm.system.XDMPermission;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.StreamSerializer;

public class XDMPermissionSerializer implements StreamSerializer<XDMPermission> {

	@Override
	public void destroy() {
	}

	@Override
	public int getTypeId() {
		return XDMDataSerializationFactory.cli_XDMPermission;
	}

	@Override
	public XDMPermission read(ObjectDataInput in) throws IOException {
		
		return new XDMPermission(in.readUTF(),
				(Set<XDMPermission.Permission>) in.readObject());
	}

	@Override
	public void write(ObjectDataOutput out, XDMPermission xPerm) throws IOException {
		
		out.writeUTF(xPerm.getResource());
		out.writeObject(xPerm.getPermissions());
	}

}
