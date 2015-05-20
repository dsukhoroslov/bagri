package com.bagri.xdm.cache.hazelcast.task.library;

import java.io.IOException;
import java.util.Date;
import java.util.Map.Entry;

import com.bagri.xdm.system.XDMLibrary;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;

public class LibFunctionCreator extends LibraryProcessor implements DataSerializable {

	private String className;
	private String prefix;
	private String description;
	private String signature;
	
	public LibFunctionCreator(int version, String admin, String className, String prefix, String description, String signature) {
		super(version, admin);
		this.className = className;
		this.prefix = prefix;
		this.description = description;
		this.signature = signature;
	}

	@Override
	public Object process(Entry<String, XDMLibrary> entry) {
		logger.debug("process.enter; entry: {}", entry); 
		if (entry.getValue() == null) {
			String name = entry.getKey();
			//XDMLibrary library = new XDMLibrary(getVersion(), new Date(), getAdmin(), 
			//		name, fileName, description, true);
			//entry.setValue(library);
			//auditEntity(AuditType.create, library);
			//return library;
		} 
		return null;
	}

	@Override
	public void readData(ObjectDataInput in) throws IOException {
		super.readData(in);
		className = in.readUTF();
		prefix = in.readUTF();
		description = in.readUTF();
		signature = in.readUTF();
	}

	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		super.writeData(out);
		out.writeUTF(className);
		out.writeUTF(prefix);
		out.writeUTF(description);
		out.writeUTF(signature);
	}

	
}
