package com.bagri.server.hazelcast.task.library;

import static com.bagri.server.hazelcast.serialize.DataSerializationFactoryImpl.cli_CreateLibraryTask;

import java.io.IOException;
import java.util.Date;
import java.util.Map.Entry;

import com.bagri.core.system.Library;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;

public class LibraryCreator extends LibraryProcessor implements IdentifiedDataSerializable {
	
	private String fileName;
	private String description;
	
	public LibraryCreator() {
		// de-ser
	}

	public LibraryCreator(String admin, String fileName, String description) {
		super(1, admin);
		this.fileName = fileName;
		this.description = description;
	}

	@Override
	public Object process(Entry<String, Library> entry) {
		logger.debug("process.enter; entry: {}", entry); 
		if (entry.getValue() == null) {
			String name = entry.getKey();
			Library library = new Library(getVersion(), new Date(), getAdmin(), 
					name, fileName, description, true);
			entry.setValue(library);
			auditEntity(AuditType.create, library);
			return library;
		} 
		return null;
	}

	@Override
	public int getId() {
		return cli_CreateLibraryTask;
	}
	
	@Override
	public void readData(ObjectDataInput in) throws IOException {
		super.readData(in);
		fileName = in.readUTF();
		description = in.readUTF();
	}

	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		super.writeData(out);
		out.writeUTF(fileName);
		out.writeUTF(description);
	}

}
