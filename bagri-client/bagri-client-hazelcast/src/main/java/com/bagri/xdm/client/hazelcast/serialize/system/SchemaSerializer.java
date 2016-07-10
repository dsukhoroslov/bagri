package com.bagri.xdm.client.hazelcast.serialize.system;

import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import com.bagri.xdm.client.hazelcast.serialize.DataSerializationFactoryImpl;
import com.bagri.xdm.domain.Element;
import com.bagri.xdm.system.Collection;
import com.bagri.xdm.system.Fragment;
import com.bagri.xdm.system.Index;
import com.bagri.xdm.system.Module;
import com.bagri.xdm.system.Schema;
import com.bagri.xdm.system.TriggerDefinition;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.StreamSerializer;

public class SchemaSerializer extends EntitySerializer implements StreamSerializer<Schema> {

	@Override
	public int getTypeId() {
		return DataSerializationFactoryImpl.cli_XDMSchema;
	}

	@Override
	public Schema read(ObjectDataInput in) throws IOException {
		Object[] entity = super.readEntity(in);
		Schema xSchema = new Schema(
				(int) entity[0],
				(Date) entity[1],
				(String) entity[2],
				in.readUTF(),
				in.readUTF(),
				in.readBoolean(),
				(Properties) in.readObject());
		int size = in.readInt();
		for (int i=0; i < size; i++) {
			Collection cln = in.readObject();
			xSchema.addCollection(cln);
		}
		size = in.readInt();
		for (int i=0; i < size; i++) {
			Fragment fgt = in.readObject();
			xSchema.addFragment(fgt);
		}
		size = in.readInt();
		for (int i=0; i < size; i++) {
			Index idx = in.readObject();
			xSchema.addIndex(idx);
		}
		size = in.readInt();
		for (int i=0; i < size; i++) {
			TriggerDefinition trg = in.readObject();
			xSchema.addTrigger(trg);
		}
		return xSchema;
	}

	@Override
	public void write(ObjectDataOutput out, Schema xSchema)	throws IOException {
		super.writeEntity(out, xSchema);
		out.writeUTF(xSchema.getName());
		out.writeUTF(xSchema.getDescription());
		out.writeBoolean(xSchema.isActive());
		out.writeObject(xSchema.getProperties());
		out.writeInt(xSchema.getCollections().size());
		for (Collection collection: xSchema.getCollections()) {
			out.writeObject(collection);
		}
		out.writeInt(xSchema.getFragments().size());
		for (Fragment fragment: xSchema.getFragments()) {
			out.writeObject(fragment);
		}
		//out.writeObject(xSchema.getIndexes());
		out.writeInt(xSchema.getIndexes().size());
		for (Index index: xSchema.getIndexes()) {
			out.writeObject(index);
		}
		out.writeInt(xSchema.getTriggers().size());
		for (TriggerDefinition trigger: xSchema.getTriggers()) {
			out.writeObject(trigger);
		}
	}

}