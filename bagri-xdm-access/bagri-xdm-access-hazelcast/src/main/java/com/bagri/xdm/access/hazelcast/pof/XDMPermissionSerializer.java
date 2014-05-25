package com.bagri.xdm.access.hazelcast.pof;

import java.io.IOException;

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
		return XDMPortableFactory.cli_XDMPermission;
	}

	@Override
	public XDMPermission read(ObjectDataInput in) throws IOException {
		
		return new XDMPermission(XDMPermission.Permission.valueOf(in.readUTF()), in.readUTF());
	}

	@Override
	public void write(ObjectDataOutput out, XDMPermission xPerm) throws IOException {
		
		out.writeUTF(xPerm.getPermission().name());
		out.writeUTF(xPerm.getResource());
	}

}
