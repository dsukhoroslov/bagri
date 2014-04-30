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
	
	private int version;

	public SchemaRemover() {
		//
	}
	
	public SchemaRemover(int version) {
		this.version = version;
	}

	@Override
	public Object process(Entry<String, XDMSchema> entry) {
		logger.debug("process.enter; entry: {}", entry); 
		if (entry.getValue() != null) {
			XDMSchema schema = entry.getValue();
			if (schema.getVersion() == version) {
				if (denitSchemaInCluster(schema) > 0) {
					schema.setActive(false);
					schema.updateVersion();
					entry.setValue(schema);
				} else {
					entry.setValue(null);
				}
				return schema;
			} else {
				// throw ex ?
				logger.warn("process; outdated schema version: {}; entry version: {}; process terminated", 
						version, entry.getValue().getVersion()); 
			}
		} 
		return null;
	}	
	
	
	@Override
	public void readData(ObjectDataInput in) throws IOException {
		version = in.readInt();
	}

	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		out.writeInt(version);
	}

	
}
