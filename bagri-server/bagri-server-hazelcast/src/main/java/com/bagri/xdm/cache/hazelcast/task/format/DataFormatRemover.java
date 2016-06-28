package com.bagri.xdm.cache.hazelcast.task.format;

import static com.bagri.xdm.cache.hazelcast.serialize.DataSerializationFactoryImpl.cli_RemoveDataFormatTask;

import java.util.Map.Entry;

import com.bagri.xdm.system.DataFormat;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;

public class DataFormatRemover extends DataFormatProcessor implements IdentifiedDataSerializable {

	public DataFormatRemover() {
		//
	}
	
	public DataFormatRemover(int version, String admin) {
		super(version, admin);
	}

	@Override
	public Object process(Entry<String, DataFormat> entry) {
		logger.debug("process.enter; entry: {}", entry); 
		if (entry.getValue() != null) {
			DataFormat format = entry.getValue();
			if (format.getVersion() == getVersion()) {
				entry.setValue(null);
				auditEntity(AuditType.delete, format);
				return format;
			} else {
				// throw ex ?
				logger.warn("process; outdated data format version: {}; entry version: {}; process terminated", 
						getVersion(), entry.getValue().getVersion()); 
			}
		} 
		return null;
	}	
	
	@Override
	public int getId() {
		return cli_RemoveDataFormatTask;
	}

}
