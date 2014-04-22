package com.bagri.xdm.process.hazelcast;

import static com.bagri.xdm.access.hazelcast.pof.XDMPortableFactory.cli_XDMInitSchemaTask;
import static com.bagri.xdm.access.hazelcast.pof.XDMPortableFactory.factoryId;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;
import java.util.concurrent.Callable;

import org.springframework.beans.factory.annotation.Autowired;

import com.bagri.xdm.access.api.XDMSchemaManagement;
import com.hazelcast.nio.serialization.Portable;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.hazelcast.spring.context.SpringAware;

@SpringAware
public class SchemaInitiator extends SchemaDenitiator {

	private Properties properties;
	
	public SchemaInitiator() {
		super();
	}

	public SchemaInitiator(String schemaName, Properties properties) {
		super(schemaName);
		this.properties = properties;
	}

	@Override
	public Boolean call() throws Exception {
		return schemaManager.initSchema(properties);
	}

	@Override
	public int getClassId() {
		return cli_XDMInitSchemaTask;
	}

	@Override
	public void readPortable(PortableReader in) throws IOException {
		// logger.trace("readPortable.enter; in: {}", in);
		super.readPortable(in);
		int size = in.readInt("size");
		properties = new Properties();
		for (int i=0; i < size; i++) {
			String key = in.readUTF("key" + i);
			String value = in.readUTF("value" + i);
			properties.setProperty(key, value);
		}
	}

	@Override
	public void writePortable(PortableWriter out) throws IOException {
		// logger.trace("writePortable.enter; out: {}", out);
		super.writePortable(out);
		out.writeInt("size", properties.size());
		Enumeration<String> props = (Enumeration<String>) properties.propertyNames();
		for (int i=0; i < properties.size(); i++) {
			String key = props.nextElement();
			out.writeUTF("key" + i, key);
			out.writeUTF("value" + i, properties.getProperty(key));
		}
	}

	
}

