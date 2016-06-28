package com.bagri.xdm.client.hazelcast.serialize.system;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.bagri.xdm.client.hazelcast.serialize.DataSerializationFactoryImpl;
import com.bagri.xdm.system.Permission;
import com.bagri.xdm.system.Role;
import com.bagri.xdm.system.User;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.StreamSerializer;

public class UserSerializer extends XDMEntitySerializer implements StreamSerializer<User> {

	@Override
	public int getTypeId() {
		return DataSerializationFactoryImpl.cli_XDMUser;
	}

	@Override
	public User read(ObjectDataInput in) throws IOException {
		Object[] entity = super.readEntity(in);
		String login = in.readUTF(); 
		String password = in.readUTF();
		boolean active = in.readBoolean();
		List<Permission> perms = (List<Permission>) in.readObject();
		Set<String> roles = (Set<String>) in.readObject();
		Map<String, Permission> mPerms = new HashMap<String, Permission>(perms.size());
		for (Permission xpm: perms) {
			mPerms.put(xpm.getResource(), xpm);
		}

		User xUser = new User(
				(int) entity[0],
				(Date) entity[1],
				(String) entity[2],
				mPerms, 
				roles,
				login,
				password,
				active);
		return xUser;
	}

	@Override
	public void write(ObjectDataOutput out, User xUser)	throws IOException {
		super.writeEntity(out, xUser);
		out.writeUTF(xUser.getLogin());
		out.writeUTF(xUser.getPassword());
		out.writeBoolean(xUser.isActive());
		// write grants and roles
		List<Permission> perms = new ArrayList<Permission>(xUser.getPermissions().values());
		out.writeObject(perms);
		out.writeObject(xUser.getIncludedRoles());
	}

}
