package com.bagri.xdm.access.hazelcast.pof;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
		String name = in.readUTF(); 
		String description = in.readUTF();
		List<XDMPermission> perms = (List<XDMPermission>) in.readObject();
		Set<String> roles = (Set<String>) in.readObject();
		Map<String, XDMPermission> mPerms = new HashMap<String, XDMPermission>(perms.size());
		for (XDMPermission xpm: perms) {
			mPerms.put(xpm.getResource(), xpm);
		}

		XDMRole xRole = new XDMRole(
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
	public void write(ObjectDataOutput out, XDMRole xRole)	throws IOException {
		super.writeEntity(out, xRole);
		out.writeUTF(xRole.getName());
		out.writeUTF(xRole.getDescription());
		// write permissions and roles
		List<XDMPermission> perms = new ArrayList<XDMPermission>(xRole.getPermissions().values());
		out.writeObject(perms);
		out.writeObject(xRole.getIncludedRoles());
	}

}
