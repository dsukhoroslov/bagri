package com.bagri.server.hazelcast.task.store;

import static com.bagri.server.hazelcast.serialize.DataSerializationFactoryImpl.cli_RemoveDataStoreTask;

import java.util.Map.Entry;

import com.bagri.core.system.DataStore;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;

public class DataStoreRemover extends DataStoreProcessor implements IdentifiedDataSerializable {

	public DataStoreRemover() {
		//
	}
	
	public DataStoreRemover(int version, String admin) {
		super(version, admin);
	}

	@Override
	public Object process(Entry<String, DataStore> entry) {
		logger.debug("process.enter; entry: {}", entry); 
		if (entry.getValue() != null) {
			DataStore store = entry.getValue();
			if (store.getVersion() == getVersion()) {
				entry.setValue(null);
				auditEntity(AuditType.delete, store);
				return store;
			} else {
				// throw ex ?
				logger.warn("process; outdated data store version: {}; entry version: {}; process terminated", 
						getVersion(), entry.getValue().getVersion()); 
			}
		} 
		return null;
	}	
	
	@Override
	public int getId() {
		return cli_RemoveDataStoreTask;
	}

}


