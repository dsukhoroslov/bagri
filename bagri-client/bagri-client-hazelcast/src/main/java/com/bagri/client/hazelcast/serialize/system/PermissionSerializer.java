package com.bagri.client.hazelcast.serialize.system;

import java.io.IOException;
import java.util.Set;

import com.bagri.client.hazelcast.serialize.DataSerializationFactoryImpl;
import com.bagri.core.system.Permission;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.StreamSerializer;

public class PermissionSerializer implements StreamSerializer<Permission> {

	@Override
	public void destroy() {
	}

	@Override
	public int getTypeId() {
		return DataSerializationFactoryImpl.cli_XDMPermission;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Permission read(ObjectDataInput in) throws IOException {
		
		return new Permission(in.readUTF(), (Set<Permission.Value>) in.readObject());
	}

	@Override
	public void write(ObjectDataOutput out, Permission xPerm) throws IOException {
		
		out.writeUTF(xPerm.getResource());
		out.writeObject(xPerm.getPermissions());
	}

}
