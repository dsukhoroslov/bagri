package com.bagri.xdm.client.hazelcast.task;

import static com.bagri.xdm.client.hazelcast.serialize.DataSerializationFactoryImpl.factoryId;

import java.io.IOException;

import com.bagri.xdm.api.XDMException;
import com.bagri.xdm.api.XDMAccessManagement;
import com.bagri.xdm.api.XDMRepository;
import com.bagri.xdm.system.Permission;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;

public abstract class ClientAwareTask implements IdentifiedDataSerializable {
	
	protected String clientId;
	protected XDMRepository repo;
	
	public ClientAwareTask() {
		//
	}
	
	public ClientAwareTask(String clientId) {
		this.clientId = clientId;
	}

	@Override
	public int getFactoryId() {
		return factoryId;
	}
	
	protected void checkPermission(Permission.Value perm) throws XDMException {
    	//repo.getXQProcessor(clientId);
    	String user = repo.getUserName();
    	if (!((XDMAccessManagement) repo.getAccessManagement()).hasPermission(user, perm)) {
    		throw new XDMException("User " + user + " has no permission to " + perm + " documents", XDMException.ecAccess);
    	}
	}

	@Override
	public void readData(ObjectDataInput in) throws IOException {
		clientId = in.readUTF();
	}

	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		out.writeUTF(clientId);
	}

	
}
