package com.bagri.server.hazelcast.task.format;

import static com.bagri.server.hazelcast.serialize.DataSerializationFactoryImpl.cli_CreateDataFormatTask;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Properties;

import com.bagri.core.system.DataFormat;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;

public class DataFormatCreator extends DataFormatProcessor implements IdentifiedDataSerializable {
	
	private String parser;
	private String builder;
	private String modeler;
	private String description;
	private String type;
	private Collection<String> extensions = new HashSet<>();
	private Properties properties = new Properties();
	
	public DataFormatCreator() {
		// de-ser
	}

	public DataFormatCreator(String admin, String parser, String builder, String modeler, String description,
			String type, Collection<String> extensions, Properties properties) {
		super(1, admin);
		this.parser = parser;
		this.builder = builder;
		this.description = description;
		this.type = type;
		if (extensions != null) {
			this.extensions.addAll(extensions);
		}
		if (properties != null) {
			this.properties.putAll(properties);
		}
	}

	@Override
	public Object process(Entry<String, DataFormat> entry) {
		logger.debug("process.enter; entry: {}", entry); 
		if (entry.getValue() == null) {
			String name = entry.getKey();
			DataFormat format = new DataFormat(getVersion(), new Date(), getAdmin(), 
					name, description, type, extensions, parser, builder, modeler, true, properties);
			entry.setValue(format);
			auditEntity(AuditType.create, format);
			return format;
		} 
		return null;
	}

	@Override
	public int getId() {
		return cli_CreateDataFormatTask;
	}
	
	@Override
	public void readData(ObjectDataInput in) throws IOException {
		super.readData(in);
		parser = in.readUTF();
		builder = in.readUTF();
		modeler = in.readUTF();
		description = in.readUTF();
		type = in.readUTF();
		extensions.addAll((Collection<String>) in.readObject());
		properties.putAll((Properties) in.readObject());
	}

	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		super.writeData(out);
		out.writeUTF(parser);
		out.writeUTF(builder);
		out.writeUTF(modeler);
		out.writeUTF(description);
		out.writeUTF(type);
		out.writeObject(extensions);
		out.writeObject(properties);
	}


}
