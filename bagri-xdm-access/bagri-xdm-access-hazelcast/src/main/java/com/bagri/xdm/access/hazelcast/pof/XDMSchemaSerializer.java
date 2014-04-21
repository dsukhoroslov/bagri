package com.bagri.xdm.access.hazelcast.pof;

import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.Properties;

import com.bagri.xdm.system.XDMSchema;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.StreamSerializer;

public class XDMSchemaSerializer implements StreamSerializer<XDMSchema> {

	@Override
	public int getTypeId() {
		return XDMPortableFactory.cli_XDMSchema;
	}

	@Override
	public void destroy() {
		// what should we do here?
	}

	@Override
	public XDMSchema read(ObjectDataInput in) throws IOException {
		
		XDMSchema xSchema = new XDMSchema(in.readUTF(),
				in.readInt(),
				in.readUTF(),
				in.readBoolean(),
				new Date(in.readLong()),
				in.readUTF(),
				(Properties) in.readObject()); 
		return xSchema;
	}

	@Override
	public void write(ObjectDataOutput out, XDMSchema xSchema)	throws IOException {
		
		out.writeUTF(xSchema.getName());
		out.writeInt(xSchema.getVersion());
		out.writeUTF(xSchema.getDescription());
		out.writeBoolean(xSchema.isActive());
		out.writeLong(xSchema.getCreatedAt().getTime());
		out.writeUTF(xSchema.getCreatedBy());
		out.writeObject(xSchema.getProperties());
	}

}
