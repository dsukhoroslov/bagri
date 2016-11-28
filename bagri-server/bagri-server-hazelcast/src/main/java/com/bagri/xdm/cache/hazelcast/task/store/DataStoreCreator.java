package com.bagri.xdm.cache.hazelcast.task.store;

import static com.bagri.xdm.cache.hazelcast.serialize.DataSerializationFactoryImpl.cli_CreateDataStoreTask;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Properties;
import java.util.Map.Entry;

import com.bagri.xdm.system.DataStore;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;

public class DataStoreCreator extends DataStoreProcessor implements IdentifiedDataSerializable {
	
	private String storeClass;
	private String description;
	private boolean readOnly;
	private Properties properties = new Properties();
	
	public DataStoreCreator() {
		// de-ser
	}

	public DataStoreCreator(String admin, String storeClass, String description, boolean readOnly, Properties properties) {
		super(1, admin);
		this.storeClass = storeClass;
		this.description = description;
		this.readOnly = readOnly;
		if (properties != null) {
			this.properties.putAll(properties);
		}
	}

	@Override
	public Object process(Entry<String, DataStore> entry) {
		logger.debug("process.enter; entry: {}", entry); 
		if (entry.getValue() == null) {
			String name = entry.getKey();
			DataStore store = new DataStore(getVersion(), new Date(), getAdmin(), 
					name, description, storeClass, readOnly, true, properties);
			entry.setValue(store);
			auditEntity(AuditType.create, store);
			return store;
		} 
		return null;
	}

	@Override
	public int getId() {
		return cli_CreateDataStoreTask;
	}
	
	@Override
	public void readData(ObjectDataInput in) throws IOException {
		super.readData(in);
		storeClass = in.readUTF();
		description = in.readUTF();
		readOnly = in.readBoolean();
		properties.putAll((Properties) in.readObject());
	}

	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		super.writeData(out);
		out.writeUTF(storeClass);
		out.writeUTF(description);
		out.writeBoolean(readOnly);
		out.writeObject(properties);
	}


}
