package com.bagri.client.hazelcast.serialize.system;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.bagri.client.hazelcast.serialize.DataSerializationFactoryImpl;
import com.bagri.core.system.Permission;
import com.bagri.core.system.Role;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.StreamSerializer;

public class RoleSerializer extends EntitySerializer implements StreamSerializer<Role> {

	@Override
	public int getTypeId() {
		return DataSerializationFactoryImpl.cli_XDMRole;
	}

	@Override
	public Role read(ObjectDataInput in) throws IOException {
		Object[] entity = super.readEntity(in);
		String name = in.readUTF(); 
		String description = in.readUTF();
		List<Permission> perms = (List<Permission>) in.readObject();
		Set<String> roles = (Set<String>) in.readObject();
		Map<String, Permission> mPerms = new HashMap<String, Permission>(perms.size());
		for (Permission xpm: perms) {
			mPerms.put(xpm.getResource(), xpm);
		}

		Role xRole = new Role(
				(int) entity[0],
				(Date) entity[1],
				(String) entity[2],
				mPerms, 
				roles,
				name, 
				description);
		return xRole;
	}

	@Override
	public void write(ObjectDataOutput out, Role xRole)	throws IOException {
		super.writeEntity(out, xRole);
		out.writeUTF(xRole.getName());
		out.writeUTF(xRole.getDescription());
		// write permissions and roles
		List<Permission> perms = new ArrayList<Permission>(xRole.getPermissions().values());
		out.writeObject(perms);
		out.writeObject(xRole.getIncludedRoles());
	}

}
