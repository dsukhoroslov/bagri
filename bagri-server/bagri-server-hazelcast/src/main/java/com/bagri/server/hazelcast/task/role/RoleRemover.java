package com.bagri.server.hazelcast.task.role;

import static com.bagri.server.hazelcast.serialize.DataSerializationFactoryImpl.cli_DeleteRoleTask;

import java.util.Map.Entry;

import com.bagri.core.system.Role;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;

public class RoleRemover extends RoleProcessor implements IdentifiedDataSerializable {

	public RoleRemover() {
		//
	}
	
	public RoleRemover(int version, String admin) {
		super(version, admin);
	}

	@Override
	public Object process(Entry<String, Role> entry) {
		logger.debug("process.enter; entry: {}", entry); 
		if (entry.getValue() != null) {
			Role role = entry.getValue();
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
	
	@Override
	public int getId() {
		return cli_DeleteRoleTask;
	}
	
	
}
