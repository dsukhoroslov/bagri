package com.bagri.xdm.process.hazelcast.role;

import java.io.IOException;
import java.util.Map.Entry;

import com.bagri.xdm.system.XDMRole;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;

public class RoleUpdater extends RoleProcessor {
	
	private Action action;
	private String[] roles;
	
	public RoleUpdater(int version, String admin, String[] roles, Action action) {
		super(version, admin);
		this.roles = roles;
		this.action = action;
	}

	@Override
	public Object process(Entry<String, XDMRole> entry) {
		logger.debug("process.enter; entry: {}", entry); 
		if (entry.getValue() != null) {
			XDMRole role = entry.getValue();
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
