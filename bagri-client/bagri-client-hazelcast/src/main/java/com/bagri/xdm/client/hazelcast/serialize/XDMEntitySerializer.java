package com.bagri.xdm.client.hazelcast.serialize;

import java.io.IOException;
import java.util.Date;

import com.bagri.xdm.common.XDMEntity;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;

public abstract class XDMEntitySerializer  {
	
	public void destroy() {
		// what should we do here?
	}

	public Object[] readEntity(ObjectDataInput in) throws IOException {
		return new Object[] {in.readInt(), new Date(in.readLong()), in.readUTF()};
	}

	public void writeEntity(ObjectDataOutput out, XDMEntity entity) throws IOException {
		out.writeInt(entity.getVersion());
		out.writeLong(entity.getCreatedAt().getTime());
		out.writeUTF(entity.getCreatedBy());
	}

}
