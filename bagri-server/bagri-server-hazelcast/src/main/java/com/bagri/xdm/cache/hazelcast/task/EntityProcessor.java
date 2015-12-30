package com.bagri.xdm.cache.hazelcast.task;

import static com.bagri.xdm.client.hazelcast.serialize.XDMDataSerializationFactory.factoryId;

import java.io.IOException;

import com.bagri.xdm.common.XDMEntity;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
//import com.hazelcast.nio.serialization.IdentifiedDataSerializable;

public abstract class EntityProcessor { //implements IdentifiedDataSerializable {
	
	protected enum AuditType {
		
		create,
		update,
		delete
	}
	
	public enum Action {
		add,
		remove
	}
	
	private int version;
	private String admin;
	
	public EntityProcessor() {
		// serialization
	}
	
	public EntityProcessor(int version, String admin) {
		this.version = version;
		this.admin = admin;
	}
	
	public int getVersion() {
		return version;
	}
	
	public String getAdmin() {
		return admin;
	}
	
	protected void auditEntity(AuditType type, XDMEntity entity) {
		// write version/admin to somewhere 
	}
	
	//@Override
	public int getFactoryId() {
		return factoryId;
	}

	//@Override
	public void readData(ObjectDataInput in) throws IOException {
		version = in.readInt();
		admin = in.readUTF();
	}

	//@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		out.writeInt(version);
		out.writeUTF(admin);
	}

}
