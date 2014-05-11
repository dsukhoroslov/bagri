package com.bagri.xdm.process.hazelcast.user;

import java.io.IOException;
import java.util.Date;
import java.util.Map.Entry;

import com.bagri.xdm.system.XDMUser;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;

public class UserCreator extends UserProcessor implements DataSerializable {
	
	private String password;

	public UserCreator(String admin, String password) {
		super(1, admin);
		this.password = password;
	}

	@Override
	public Object process(Entry<String, XDMUser> entry) {
		logger.debug("process.enter; entry: {}", entry); 
		if (entry.getValue() == null) {
			String login = entry.getKey();
			XDMUser user = new XDMUser(login, password, true, getVersion(),  
					new Date(), getAdmin());
			entry.setValue(user);
			auditEntity(AuditType.create, user);
			return user;
		} 
		return null;
	}

	@Override
	public void readData(ObjectDataInput in) throws IOException {
		super.readData(in);
		password = in.readUTF();
	}

	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		super.writeData(out);
		out.writeUTF(password);
	}

}
