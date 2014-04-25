package com.bagri.xdm.process.hazelcast.schema;

import java.io.IOException;
import java.util.Date;
import java.util.Properties;
import java.util.Map.Entry;

import com.bagri.xdm.system.XDMSchema;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;

public class SchemaUpdater extends SchemaProcessor implements DataSerializable {

	private int version;
	private boolean override;
	private Properties properties;
	
	public SchemaUpdater() {
		//
	}
	
	public SchemaUpdater(int version, boolean override, Properties properties) {
		this.version = version;
		this.override = override;
		this.properties = properties;
	}

	@Override
	public Object process(Entry<String, XDMSchema> entry) {
		logger.debug("process.enter; entry: {}", entry); 
		if (entry.getValue() != null) {
			XDMSchema schema = entry.getValue();
			if (schema.getVersion() == version) {
				if (schema.isActive()) {
					if (denitSchemaInCluster(schema) > 0) {
						// don't go further
						return null;
					}
				}
				
				if (override) {
					schema.setProperties(properties);
				} else {
					for (String name: properties.stringPropertyNames()) {
						schema.setProperty(name, properties.getProperty(name));
					}
				}
				
				if (schema.isActive()) {
					if (initSchemaInCluster(schema) == 0) {
						schema.setActive(false);
					}
				}
				schema.updateVersion();
				entry.setValue(schema);
				return schema;
			}
		} 
		return null;
	}

	@Override
	public void readData(ObjectDataInput in) throws IOException {
		// logger.trace("readPortable.enter; in: {}", in);
		version = in.readInt();
		override = in.readBoolean();
		properties = in.readObject();
	}

	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		// logger.trace("writePortable.enter; out: {}", out);
		out.writeInt(version);
		out.writeBoolean(override);
		out.writeObject(properties);
	}



}
