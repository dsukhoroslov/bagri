package com.bagri.xdm.process.hazelcast;

import java.io.IOException;

import com.bagri.xdm.api.XDMEntity;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;

public abstract class EntityProcessor {
	
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
	
	public void readData(ObjectDataInput in) throws IOException {
		version = in.readInt();
		admin = in.readUTF();
	}

	public void writeData(ObjectDataOutput out) throws IOException {
		out.writeInt(version);
		out.writeUTF(admin);
	}

}
