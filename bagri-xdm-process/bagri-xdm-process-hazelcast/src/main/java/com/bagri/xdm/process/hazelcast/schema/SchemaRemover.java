package com.bagri.xdm.process.hazelcast.schema;

import java.io.IOException;
import java.util.Map.Entry;

import com.bagri.xdm.system.XDMSchema;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;
import com.hazelcast.spring.context.SpringAware;

@SpringAware
public class SchemaRemover extends SchemaProcessor implements DataSerializable {
	
	public SchemaRemover() {
		//
	}
	
	public SchemaRemover(int version, String admin) {
		super(version, admin);
	}

	@Override
	public Object process(Entry<String, XDMSchema> entry) {
		logger.debug("process.enter; entry: {}", entry); 
		if (entry.getValue() != null) {
			XDMSchema schema = entry.getValue();
			if (schema.getVersion() == getVersion()) {
				if (denitSchemaInCluster(schema) > 0) {
					schema.setActive(false);
					schema.updateVersion(getAdmin());
					entry.setValue(schema);
				} else {
					entry.setValue(null);
				}
				auditEntity(AuditType.delete, schema);
				return schema;
			} else {
				// throw ex ?
				logger.warn("process; outdated schema version: {}; entry version: {}; process terminated", 
						getVersion(), entry.getValue().getVersion()); 
			}
		} 
		return null;
	}	
	
}
