package com.bagri.server.hazelcast.task.user;

import static com.bagri.server.hazelcast.serialize.DataSerializationFactoryImpl.cli_CreateUserTask;

import java.io.IOException;
import java.util.Date;
import java.util.Map.Entry;

import com.bagri.core.system.User;
import com.bagri.support.security.Encryptor;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;

public class UserCreator extends UserProcessor implements IdentifiedDataSerializable {
	
	private String password;
	
	public UserCreator() {
		//de-ser
	}

	public UserCreator(String admin, String password) {
		super(1, admin);
		this.password = password;
	}

	@Override
	public Object process(Entry<String, User> entry) {
		logger.debug("process.enter; entry: {}", entry); 
		if (entry.getValue() == null) {
			String login = entry.getKey();
			String pwd = Encryptor.encrypt(password);
			User user = new User(getVersion(), new Date(), getAdmin(), null, null, login, pwd, true);
			entry.setValue(user);
			auditEntity(AuditType.create, user);
			return user;
		} 
		return null;
	}

	@Override
	public int getId() {
		return cli_CreateUserTask;
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
