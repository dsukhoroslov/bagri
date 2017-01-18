package com.bagri.server.hazelcast.serialize.system;

import java.io.IOException;
import java.util.Date;

import com.bagri.core.system.Entity;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;

public abstract class EntitySerializer  {
	
	public void destroy() {
		// what should we do here?
	}

	public Object[] readEntity(ObjectDataInput in) throws IOException {
		return new Object[] {in.readInt(), new Date(in.readLong()), in.readUTF()};
	}

	public void writeEntity(ObjectDataOutput out, Entity entity) throws IOException {
		out.writeInt(entity.getVersion());
		out.writeLong(entity.getCreatedAt().getTime());
		out.writeUTF(entity.getCreatedBy());
	}

}
