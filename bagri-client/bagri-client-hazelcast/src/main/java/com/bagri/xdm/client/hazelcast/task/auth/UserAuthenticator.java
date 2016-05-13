package com.bagri.xdm.client.hazelcast.task.auth;

import static com.bagri.xdm.client.hazelcast.serialize.DataSerializationFactoryImpl.cli_AuthenticateTask;
import static com.bagri.xdm.client.hazelcast.serialize.DataSerializationFactoryImpl.factoryId;

import java.io.IOException;
import java.util.concurrent.Callable;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;

public class UserAuthenticator implements Callable<Boolean>, IdentifiedDataSerializable {
	
	protected String userName;
	protected byte[] password;
	
	public UserAuthenticator() {
		// de-ser
	}
	
	public UserAuthenticator(String userName, String password) {
		this.userName = userName;
		this.password = password.getBytes();
	}

	@Override
	public Boolean call() throws Exception {
		return null;
	}

	@Override
	public int getFactoryId() {
		return factoryId;
	}

	@Override
	public int getId() {
		return cli_AuthenticateTask;
	}

	@Override
	public void readData(ObjectDataInput in) throws IOException {
		userName = in.readUTF();
		password = in.readByteArray();
	}

	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		out.writeUTF(userName);
		out.write(password);
	}


}
