package com.bagri.client.hazelcast.task;

import static com.bagri.client.hazelcast.serialize.TaskSerializationFactory.cli_factory_id;

import java.io.IOException;

import com.bagri.core.api.AccessManagement;
import com.bagri.core.api.SchemaRepository;
import com.bagri.core.api.impl.SchemaRepositoryBase;
import com.bagri.core.api.BagriException;
import com.bagri.core.system.Permission;
import com.bagri.support.pool.ContentDataPool;
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
		((SchemaRepositoryBase) repo).setClientId(clientId);
    	repo.getAccessManagement().checkPermission(clientId, perm);
	}

	public void setRepository(SchemaRepository repo) {
		this.repo = repo;
	}
	
	@Override
	public void readData(ObjectDataInput in) throws IOException {
		ContentDataPool cdPool = ContentDataPool.getDataPool();
		clientId = cdPool.intern(in.readUTF());
	}

	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		out.writeUTF(clientId);
	}

	
}
