package com.bagri.xdm.cache.hazelcast.task.role;

import static com.bagri.xdm.cache.hazelcast.serialize.DataSerializationFactoryImpl.cli_UpdateRoleTask;

import java.io.IOException;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.xdm.cache.hazelcast.task.EntityProcessor;
import com.bagri.xdm.system.PermissionAware;
import com.bagri.xdm.system.Role;
import com.hazelcast.map.EntryBackupProcessor;
import com.hazelcast.map.EntryProcessor;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;

public class RoleUpdater extends EntityProcessor implements EntryProcessor<String, PermissionAware>, 
	EntryBackupProcessor<String, PermissionAware>, IdentifiedDataSerializable {

	private static final transient Logger logger = LoggerFactory.getLogger(RoleUpdater.class);
	
	private Action action;
	private String[] roles;
	
	public RoleUpdater() {
		// de-ser
	}
	
	public RoleUpdater(int version, String admin, String[] roles, Action action) {
		super(version, admin);
		this.roles = roles;
		this.action = action;
	}

	@Override
	public void processBackup(Entry<String, PermissionAware> entry) {
		process(entry);		
	}

	@Override
	public EntryBackupProcessor<String, PermissionAware> getBackupProcessor() {
		return this;
	}
	
	@Override
	public Object process(Entry<String, PermissionAware> entry) {
		logger.debug("process.enter; entry: {}", entry); 
		if (entry.getValue() != null) {
			PermissionAware role = entry.getValue();
			if (role.getVersion() == getVersion()) {
				// check for circular inclusion !!
				if (action == Action.add) {
					for (String name: roles) {
						if (!role.addIncludedRole(name)) {
							logger.warn("process.add; role {} already present in the included role set, skipped;", name); 
						}
					}
				} else {
					for (String name: roles) {
						if (!role.removeIncludedRole(name)) {
							logger.warn("process.remove; role {} not present in the included role set, skipped;", name); 
						}
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
	public int getId() {
		return cli_UpdateRoleTask;
	}

	@Override
	public void readData(ObjectDataInput in) throws IOException {
		super.readData(in);
		action = Action.valueOf(in.readUTF());
		roles = (String[]) in.readObject();
	}

	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		super.writeData(out);
		out.writeUTF(action.name());
		out.writeObject(roles);
	}

}
