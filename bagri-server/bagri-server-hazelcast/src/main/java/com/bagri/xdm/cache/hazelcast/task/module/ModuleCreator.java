package com.bagri.xdm.cache.hazelcast.task.module;

import static com.bagri.xdm.cache.hazelcast.serialize.DataSerializationFactoryImpl.cli_CreateModuleTask;

import java.io.IOException;
import java.util.Date;
import java.util.Map.Entry;

//import com.bagri.xdm.cache.hazelcast.task.EntityProcessor.AuditType;
import com.bagri.xdm.system.Module;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;

public class ModuleCreator extends ModuleProcessor implements IdentifiedDataSerializable {
	
	private String fileName;
	private String prefix; 
	private String namespace;
	private String description;
	
	public ModuleCreator() {
		// de-ser
	}

	public ModuleCreator(String admin, String fileName, String prefix, String namespace, String description) {
		super(1, admin);
		this.fileName = fileName;
		this.prefix = prefix;
		this.namespace = namespace;
		this.description = description;
	}

	@Override
	public Object process(Entry<String, Module> entry) {
		logger.debug("process.enter; entry: {}", entry); 
		if (entry.getValue() == null) {
			String name = entry.getKey();
			String body = "module namespace ns = \"" + namespace + "\";";
			Module module = new Module(getVersion(), new Date(), getAdmin(), 
					name, fileName, description, prefix, namespace, body, true);
			entry.setValue(module);
			auditEntity(AuditType.create, module);
			return module;
		} 
		return null;
	}

	@Override
	public int getId() {
		return cli_CreateModuleTask;
	}
	
	@Override
	public void readData(ObjectDataInput in) throws IOException {
		super.readData(in);
		fileName = in.readUTF();
		prefix = in.readUTF();
		namespace = in.readUTF();
		description = in.readUTF();
	}

	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		super.writeData(out);
		out.writeUTF(fileName);
		out.writeUTF(prefix);
		out.writeUTF(namespace);
		out.writeUTF(description);
	}

}
