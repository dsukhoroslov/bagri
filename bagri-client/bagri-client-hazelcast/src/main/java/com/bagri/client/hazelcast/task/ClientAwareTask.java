package com.bagri.client.hazelcast.task;

import static com.bagri.client.hazelcast.serialize.TaskSerializationFactory.cli_factory_id;

import java.io.IOException;

import com.bagri.core.api.AccessManagement;
import com.bagri.core.api.SchemaRepository;
import com.bagri.core.api.BagriException;
import com.bagri.core.system.Permission;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;

public abstract class ClientAwareTask implements IdentifiedDataSerializable {
	
	protected String clientId;
	protected SchemaRepository repo;
	
	public ClientAwareTask() {
		//
	}
	
	public ClientAwareTask(String clientId) {
		this.clientId = clientId;
	}

	@Override
	public int getFactoryId() {
		return cli_factory_id;
	}
	
	protected void checkPermission(Permission.Value perm) throws BagriException {
    	//repo.getXQProcessor(clientId);
    	String user = repo.getUserName();
    	if (!((AccessManagement) repo.getAccessManagement()).hasPermission(user, perm)) {
    		throw new BagriException("User " + user + " has no permission to " + perm + " documents", BagriException.ecAccess);
    	}
	}

	public void setRepository(SchemaRepository repo) {
		this.repo = repo;
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
