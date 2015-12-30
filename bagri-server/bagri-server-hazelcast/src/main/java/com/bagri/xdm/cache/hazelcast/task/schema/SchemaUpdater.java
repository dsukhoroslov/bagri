package com.bagri.xdm.cache.hazelcast.task.schema;

import static com.bagri.common.security.Encryptor.encrypt;
import static com.bagri.xdm.client.hazelcast.serialize.XDMDataSerializationFactory.cli_UpdateSchemaTask;
import static com.bagri.xdm.common.XDMConstants.pn_schema_password;

import java.io.IOException;
import java.util.Properties;
import java.util.Map.Entry;

import com.bagri.xdm.system.XDMSchema;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;

public class SchemaUpdater extends SchemaProcessor implements IdentifiedDataSerializable {

	private boolean override;
	private Properties properties;
	
	public SchemaUpdater() {
		//
	}
	
	public SchemaUpdater(int version, String admin, boolean override, Properties properties) {
		super(version, admin);
		this.override = override;
		this.properties = properties;
	}

	@Override
	public Object process(Entry<String, XDMSchema> entry) {
		logger.debug("process.enter; entry: {}", entry); 
		if (entry.getValue() != null) {
			XDMSchema schema = entry.getValue();
			if (schema.getVersion() == getVersion()) {
				if (schema.isActive()) {
					if (denitSchemaInCluster(schema) > 0) {
						// don't go further
						return null;
					}
				}
				
				if (override) {
					String pwd = properties.getProperty(pn_schema_password);
					if (pwd != null) {
						properties.setProperty(pn_schema_password, encrypt(pwd));
					}
					schema.setProperties(properties);
				} else {
					for (String name: properties.stringPropertyNames()) {
						String value = properties.getProperty(name);
						if (pn_schema_password.equals(name)) {
							value = encrypt(value);
						}
						schema.setProperty(name, value);
					}
				}
				
				if (schema.isActive()) {
					if (initSchemaInCluster(schema) == 0) {
						schema.setActive(false);
					}
				}
				schema.updateVersion(getAdmin());
				entry.setValue(schema);
				auditEntity(AuditType.update, schema);
				return schema;
			}
		} 
		return null;
	}

	@Override
	public int getId() {
		return cli_UpdateSchemaTask;
	}
	
	@Override
	public void readData(ObjectDataInput in) throws IOException {
		super.readData(in);
		override = in.readBoolean();
		properties = in.readObject();
	}

	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		super.writeData(out);
		out.writeBoolean(override);
		out.writeObject(properties);
	}

}
