package com.bagri.xdm.process.hazelcast.role;

import java.io.IOException;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.xdm.process.hazelcast.EntityProcessor;
import com.bagri.xdm.system.XDMPermissionAware;
import com.bagri.xdm.system.XDMRole;
import com.hazelcast.map.EntryBackupProcessor;
import com.hazelcast.map.EntryProcessor;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;

public class RoleUpdater extends EntityProcessor implements EntryProcessor<String, XDMPermissionAware>, 
	EntryBackupProcessor<String, XDMPermissionAware> {

	private static final transient Logger logger = LoggerFactory.getLogger(RoleUpdater.class);
	
	private Action action;
	private String[] roles;
	
	public RoleUpdater(int version, String admin, String[] roles, Action action) {
		super(version, admin);
		this.roles = roles;
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
