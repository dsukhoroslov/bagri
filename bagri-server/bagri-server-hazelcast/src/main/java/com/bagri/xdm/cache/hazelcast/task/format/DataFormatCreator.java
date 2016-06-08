package com.bagri.xdm.cache.hazelcast.task.format;

import static com.bagri.xdm.cache.hazelcast.serialize.DataSerializationFactoryImpl.cli_CreateDataFormatTask;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Properties;

import com.bagri.xdm.system.XDMDataFormat;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;

public class DataFormatCreator extends DataFormatProcessor implements IdentifiedDataSerializable {
	
	private String parser;
	private String builder;
	private String description;
	private Collection<String> extensions = new HashSet<>();
	private Properties properties = new Properties();
	
	public DataFormatCreator() {
		// de-ser
	}

	public DataFormatCreator(String admin, String parser, String builder, String description,
			Collection<String> extensions, Properties properties) {
		super(1, admin);
		this.parser = parser;
		this.builder = builder;
		this.description = description;
		if (extensions != null) {
			this.extensions.addAll(extensions);
		}
		if (properties != null) {
			this.properties.putAll(properties);
		}
	}

	@Override
	public Object process(Entry<String, XDMDataFormat> entry) {
		logger.debug("process.enter; entry: {}", entry); 
		if (entry.getValue() == null) {
			String name = entry.getKey();
			XDMDataFormat format = new XDMDataFormat(getVersion(), new Date(), getAdmin(), 
					name, description, extensions, parser, builder, true, properties);
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
		description = in.readUTF();
		extensions.addAll((Collection<String>) in.readObject());
		properties.putAll((Properties) in.readObject());
	}

	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		super.writeData(out);
		out.writeUTF(parser);
		out.writeUTF(builder);
		out.writeUTF(description);
		out.writeObject(extensions);
		out.writeObject(properties);
	}


}
