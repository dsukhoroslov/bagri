package com.bagri.xdm.process.hazelcast.role;

import java.io.IOException;
import java.util.Map.Entry;

import com.bagri.xdm.system.XDMPermission;
import com.bagri.xdm.system.XDMPermission.Permission;
import com.bagri.xdm.system.XDMRole;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;

public class PermissionUpdater extends RoleProcessor {

	private Action action;
	private String resource;
	private String[] permissions;
	
	public PermissionUpdater(int version, String admin, String resource, String[] permissions, Action action) {
		super(version, admin);
		this.resource = resource;
		this.permissions = permissions;
		this.action = action;
	}

	@Override
	public Object process(Entry<String, XDMRole> entry) {
		logger.debug("process.enter; entry: {}", entry); 
		if (entry.getValue() != null) {
			XDMRole role = entry.getValue();
			if (role.getVersion() == getVersion()) {
				XDMPermission perm = role.getPermissions().get(resource);
				if (perm != null) {
					if (action == Action.add) {
						for (String permission: permissions) {
							if (!perm.addPermission(Permission.valueOf(permission))) {
								logger.warn("process.add; permission {} already granted for resource {}, skipped;", 
										permission, resource); 
							}
						}
					} else {
						if (permissions.length > 0) {
							for (String permission: permissions) {
								if (!perm.removePermission(Permission.valueOf(permission))) {
									logger.warn("process.remove; permission {} not granted for resource {}, skipped;", 
											permission, resource); 
								}
							}
						} else {
							role.getPermissions().remove(resource);
						}
					}
				} else {
					perm = new XDMPermission(resource);
					for (String permission: permissions) {
						perm.addPermission(Permission.valueOf(permission));
					}
					role.getPermissions().put(resource, perm);
				}
				role.updateVersion(getAdmin());
				entry.setValue(role);
				auditEntity(AuditType.update, role);
				return role;
			} else {
				// throw ex ?
				logger.warn("process; outdated role version: {}; entry version: {}; process terminated", 
						getVersion(), entry.getValue().getVersion()); 
			}
		} 
		return null;
	}

	@Override
	public void readData(ObjectDataInput in) throws IOException {
		super.readData(in);
		action = Action.valueOf(in.readUTF());
		resource = in.readUTF();
		permissions = (String[]) in.readObject();
	}

	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		super.writeData(out);
		out.writeUTF(action.name());
		out.writeUTF(resource);
		out.writeObject(permissions);
	}

}
