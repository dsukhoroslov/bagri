package com.bagri.xdm.process.hazelcast.schema;

import java.io.IOException;
import java.util.Map.Entry;

import com.bagri.xdm.system.XDMSchema;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;
import com.hazelcast.spring.context.SpringAware;

@SpringAware
public class SchemaActivator extends SchemaProcessor implements DataSerializable {
	
	private int version;
	private boolean activate;
	
	public SchemaActivator() {
		//
	}
	
	public SchemaActivator(int version, boolean activate) {
		this.version = version;
		this.activate = activate;
	}

	@Override
	public Object process(Entry<String, XDMSchema> entry) {
		logger.debug("process.enter; entry: {}", entry);
		Object result = null;
		if (entry.getValue() != null) {
			XDMSchema schema = entry.getValue();
			if (schema.getVersion() == version) {
				if (activate) {
					if (!schema.isActive()) {
						if (initSchemaInCluster(schema) > 0) {
							schema.setActive(true);
							schema.updateVersion();
							entry.setValue(schema);
							result = schema;
						}
					}
				} else {
					if (schema.isActive()) {
						if (denitSchemaInCluster(schema) == 0) {
							schema.setActive(false);
							schema.updateVersion();
							entry.setValue(schema);
							result = schema;
						}
					}
				}
			}
		} 
		logger.debug("process.exit; returning: {}", result); 
		return result;
	}

	@Override
	public void readData(ObjectDataInput in) throws IOException {
		version = in.readInt();
		activate = in.readBoolean();
	}

	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		out.writeInt(version);
		out.writeBoolean(activate);
	}


}
