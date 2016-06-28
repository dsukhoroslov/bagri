package com.bagri.xdm.cache.hazelcast.task.role;

import static com.bagri.xdm.cache.hazelcast.serialize.DataSerializationFactoryImpl.cli_CreateRoleTask;

import java.io.IOException;
import java.util.Date;
import java.util.Map.Entry;

import com.bagri.xdm.system.Role;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;

public class RoleCreator extends RoleProcessor implements IdentifiedDataSerializable {
	
	private String description;
	
	public RoleCreator() {
		// de-ser
	}

	public RoleCreator(String admin, String description) {
		super(1, admin);
		this.description = description;
	}

	@Override
	public Object process(Entry<String, Role> entry) {
		logger.debug("process.enter; entry: {}", entry); 
		if (entry.getValue() == null) {
			String name = entry.getKey();
			Role role = new Role(getVersion(), new Date(), getAdmin(), null, null, name, description);
			entry.setValue(role);
			auditEntity(AuditType.create, role);
			return role;
		} 
		return null;
	}

	@Override
	public int getId() {
		return cli_CreateRoleTask;
	}
	
	@Override
	public void readData(ObjectDataInput in) throws IOException {
		super.readData(in);
		description = in.readUTF();
	}

	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		super.writeData(out);
		out.writeUTF(description);
	}


}
