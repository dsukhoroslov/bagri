package com.bagri.xdm.cache.hazelcast.task.library;

import java.util.Map.Entry;

import com.bagri.xdm.system.XDMLibrary;
import com.hazelcast.nio.serialization.DataSerializable;

public class LibraryRemover extends LibraryProcessor implements DataSerializable {

	public LibraryRemover() {
		//
	}
	
	public LibraryRemover(int version, String admin) {
		super(version, admin);
	}

	@Override
	public Object process(Entry<String, XDMLibrary> entry) {
		logger.debug("process.enter; entry: {}", entry); 
		if (entry.getValue() != null) {
			XDMLibrary library = entry.getValue();
			if (library.getVersion() == getVersion()) {
				entry.setValue(null);
				auditEntity(AuditType.delete, library);
				return library;
			} else {
				// throw ex ?
				logger.warn("process; outdated library version: {}; entry version: {}; process terminated", 
						getVersion(), entry.getValue().getVersion()); 
			}
		} 
		return null;
	}	


}
