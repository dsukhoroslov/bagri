package com.bagri.xdm.cache.hazelcast.task.user;

import java.util.Map.Entry;

import com.bagri.xdm.system.XDMUser;
import com.hazelcast.nio.serialization.DataSerializable;

public class UserRemover extends UserProcessor implements DataSerializable {

	public UserRemover() {
		//
	}
	
	public UserRemover(int version, String admin) {
		super(version, admin);
	}

	@Override
	public Object process(Entry<String, XDMUser> entry) {
		logger.debug("process.enter; entry: {}", entry); 
		if (entry.getValue() != null) {
			XDMUser user = entry.getValue();
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
	
	
}
