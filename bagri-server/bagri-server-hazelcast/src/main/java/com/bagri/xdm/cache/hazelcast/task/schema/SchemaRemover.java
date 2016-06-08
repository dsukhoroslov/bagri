package com.bagri.xdm.cache.hazelcast.task.schema;

import static com.bagri.xdm.cache.hazelcast.serialize.DataSerializationFactoryImpl.cli_DeleteSchemaTask;

import java.util.Map.Entry;

import com.bagri.xdm.system.XDMSchema;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;
import com.hazelcast.spring.context.SpringAware;

@SpringAware
public class SchemaRemover extends SchemaProcessor implements IdentifiedDataSerializable {
	
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
	
	@Override
	public int getId() {
		return cli_DeleteSchemaTask;
	}
	
	
	
}
