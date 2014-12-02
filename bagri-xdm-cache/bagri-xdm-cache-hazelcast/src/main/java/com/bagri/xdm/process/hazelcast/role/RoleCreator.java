package com.bagri.xdm.process.hazelcast.role;

import java.io.IOException;
import java.util.Date;
import java.util.Map.Entry;

import com.bagri.xdm.system.XDMRole;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;

public class RoleCreator extends RoleProcessor implements DataSerializable {
	
	private String description;

	public RoleCreator(String admin, String description) {
		super(1, admin);
		this.description = description;
	}

	@Override
	public Object process(Entry<String, XDMRole> entry) {
		logger.debug("process.enter; entry: {}", entry); 
		if (entry.getValue() == null) {
			String name = entry.getKey();
			XDMRole role = new XDMRole(getVersion(), new Date(), getAdmin(), null, null, name, description);
			entry.setValue(role);
			auditEntity(AuditType.create, role);
			return role;
		} 
		return null;
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
