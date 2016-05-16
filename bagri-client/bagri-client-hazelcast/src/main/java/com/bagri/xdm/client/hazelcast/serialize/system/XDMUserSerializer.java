package com.bagri.xdm.client.hazelcast.serialize.system;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.bagri.xdm.client.hazelcast.serialize.DataSerializationFactoryImpl;
import com.bagri.xdm.system.XDMPermission;
import com.bagri.xdm.system.XDMRole;
import com.bagri.xdm.system.XDMUser;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.StreamSerializer;

public class XDMUserSerializer extends XDMEntitySerializer implements StreamSerializer<XDMUser> {

	@Override
	public int getTypeId() {
		return DataSerializationFactoryImpl.cli_XDMUser;
	}

	@Override
	public XDMUser read(ObjectDataInput in) throws IOException {
		Object[] entity = super.readEntity(in);
		String login = in.readUTF(); 
		String password = in.readUTF();
		boolean active = in.readBoolean();
		List<XDMPermission> perms = (List<XDMPermission>) in.readObject();
		Set<String> roles = (Set<String>) in.readObject();
		Map<String, XDMPermission> mPerms = new HashMap<String, XDMPermission>(perms.size());
		for (XDMPermission xpm: perms) {
			mPerms.put(xpm.getResource(), xpm);
		}

		XDMUser xUser = new XDMUser(
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
	public void write(ObjectDataOutput out, XDMUser xUser)	throws IOException {
		super.writeEntity(out, xUser);
		out.writeUTF(xUser.getLogin());
		out.writeUTF(xUser.getPassword());
		out.writeBoolean(xUser.isActive());
		// write grants and roles
		List<XDMPermission> perms = new ArrayList<XDMPermission>(xUser.getPermissions().values());
		out.writeObject(perms);
		out.writeObject(xUser.getIncludedRoles());
	}

}
