package com.bagri.xdm.cache.hazelcast.task.user;

import static com.bagri.xdm.cache.hazelcast.serialize.DataSerializationFactoryImpl.cli_DeleteUserTask;

import java.util.Map.Entry;

import com.bagri.xdm.system.User;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;

public class UserRemover extends UserProcessor implements IdentifiedDataSerializable {

	public UserRemover() {
		//
	}
	
	public UserRemover(int version, String admin) {
		super(version, admin);
	}

	@Override
	public Object process(Entry<String, User> entry) {
		logger.debug("process.enter; entry: {}", entry); 
		if (entry.getValue() != null) {
			User user = entry.getValue();
			if (user.getVersion() == getVersion()) {
				entry.setValue(null);
				auditEntity(AuditType.delete, user);
				return user;
			} else {
				// throw ex ?
				logger.warn("process; outdated user version: {}; entry version: {}; process terminated", 
						getVersion(), entry.getValue().getVersion()); 
			}
		} 
		return null;
	}	
	
	@Override
	public int getId() {
		return cli_DeleteUserTask;
	}
	
}
