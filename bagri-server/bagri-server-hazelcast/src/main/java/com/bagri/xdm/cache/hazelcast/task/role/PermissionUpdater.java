package com.bagri.xdm.cache.hazelcast.task.role;

import java.io.IOException;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.xdm.cache.hazelcast.task.EntityProcessor;
import com.bagri.xdm.system.XDMPermission;
import com.bagri.xdm.system.XDMPermissionAware;
import com.bagri.xdm.system.XDMPermission.Permission;
import com.hazelcast.map.EntryBackupProcessor;
import com.hazelcast.map.EntryProcessor;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;

public class PermissionUpdater extends EntityProcessor implements EntryProcessor<String, XDMPermissionAware>, 
	EntryBackupProcessor<String, XDMPermissionAware> {

	private static final transient Logger logger = LoggerFactory.getLogger(RoleUpdater.class);

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
	public void processBackup(Entry<String, XDMPermissionAware> entry) {
		process(entry);		
	}

	@Override
	public EntryBackupProcessor<String, XDMPermissionAware> getBackupProcessor() {
		return this;
	}
	
	@Override
	public Object process(Entry<String, XDMPermissionAware> entry) {
		logger.debug("process.enter; entry: {}", entry); 
		if (entry.getValue() != null) {
			XDMPermissionAware role = entry.getValue();
			if (role.getVersion() == getVersion()) {
				if (action == Action.add) {
					for (String permission: permissions) {
						if (!role.addPermission(resource, Permission.valueOf(permission))) {
							logger.warn("process.add; permission {} already granted for resource {}, skipped;", 
									permission, resource); 
						}
					} 
				} else {
					if (permissions.length > 0) {
						for (String permission: permissions) {
							if (!role.removePermission(resource, Permission.valueOf(permission))) {
								logger.warn("process.remove; permission {} not granted for resource {}, skipped;", 
										permission, resource); 
							}
						} 
					} else {
						role.removePermission(resource, null);
					}
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
