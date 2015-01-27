package com.bagri.xdm.cache.hazelcast.task.role;

import java.util.Map.Entry;

import com.bagri.xdm.system.XDMRole;
import com.hazelcast.nio.serialization.DataSerializable;

public class RoleRemover extends RoleProcessor implements DataSerializable {

	public RoleRemover() {
		//
	}
	
	public RoleRemover(int version, String admin) {
		super(version, admin);
	}

	@Override
	public Object process(Entry<String, XDMRole> entry) {
		logger.debug("process.enter; entry: {}", entry); 
		if (entry.getValue() != null) {
			XDMRole role = entry.getValue();
			if (role.getVersion() == getVersion()) {
				entry.setValue(null);
				auditEntity(AuditType.delete, role);
				return role;
			} else {
				// throw ex ?
				logger.warn("process; outdated role version: {}; entry version: {}; process terminated", 
						getVersion(), entry.getValue().getVersion()); 
			}
		} 
		return null;
	}	
	
	
}
