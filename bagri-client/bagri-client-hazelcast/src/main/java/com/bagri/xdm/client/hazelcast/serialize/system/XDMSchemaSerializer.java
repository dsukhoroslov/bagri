package com.bagri.xdm.client.hazelcast.serialize.system;

import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import com.bagri.xdm.client.hazelcast.serialize.DataSerializationFactoryImpl;
import com.bagri.xdm.domain.XDMElement;
import com.bagri.xdm.system.XDMCollection;
import com.bagri.xdm.system.XDMFragment;
import com.bagri.xdm.system.XDMIndex;
import com.bagri.xdm.system.XDMModule;
import com.bagri.xdm.system.XDMSchema;
import com.bagri.xdm.system.XDMTriggerDef;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.StreamSerializer;

public class XDMSchemaSerializer extends XDMEntitySerializer implements StreamSerializer<XDMSchema> {

	@Override
	public int getTypeId() {
		return DataSerializationFactoryImpl.cli_XDMSchema;
	}

	@Override
	public XDMSchema read(ObjectDataInput in) throws IOException {
		Object[] entity = super.readEntity(in);
		XDMSchema xSchema = new XDMSchema(
				(int) entity[0],
				(Date) entity[1],
				(String) entity[2],
				in.readUTF(),
				in.readUTF(),
				in.readBoolean(),
				(Properties) in.readObject());
		int size = in.readInt();
		for (int i=0; i < size; i++) {
			XDMCollection cln = in.readObject();
			xSchema.addCollection(cln);
		}
		size = in.readInt();
		for (int i=0; i < size; i++) {
			XDMFragment fgt = in.readObject();
			xSchema.addFragment(fgt);
		}
		size = in.readInt();
		for (int i=0; i < size; i++) {
			XDMIndex idx = in.readObject();
			xSchema.addIndex(idx);
		}
		size = in.readInt();
		for (int i=0; i < size; i++) {
			XDMTriggerDef trg = in.readObject();
			xSchema.addTrigger(trg);
		}
		return xSchema;
	}

	@Override
	public void write(ObjectDataOutput out, XDMSchema xSchema)	throws IOException {
		super.writeEntity(out, xSchema);
		out.writeUTF(xSchema.getName());
		out.writeUTF(xSchema.getDescription());
		out.writeBoolean(xSchema.isActive());
		out.writeObject(xSchema.getProperties());
		out.writeInt(xSchema.getCollections().size());
		for (XDMCollection collection: xSchema.getCollections()) {
			out.writeObject(collection);
		}
		out.writeInt(xSchema.getFragments().size());
		for (XDMFragment fragment: xSchema.getFragments()) {
			out.writeObject(fragment);
		}
		//out.writeObject(xSchema.getIndexes());
		out.writeInt(xSchema.getIndexes().size());
		for (XDMIndex index: xSchema.getIndexes()) {
			out.writeObject(index);
		}
		out.writeInt(xSchema.getTriggers().size());
		for (XDMTriggerDef trigger: xSchema.getTriggers()) {
			out.writeObject(trigger);
		}
	}

}
