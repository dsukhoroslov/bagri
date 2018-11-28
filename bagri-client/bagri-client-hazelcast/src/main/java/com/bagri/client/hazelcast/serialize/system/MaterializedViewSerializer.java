package com.bagri.client.hazelcast.serialize.system;

import java.io.IOException;
import java.util.Date;

import com.bagri.client.hazelcast.serialize.DomainSerializationFactory;
import com.bagri.core.system.MaterializedView;
import com.bagri.core.system.Parameter;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.StreamSerializer;

public class MaterializedViewSerializer extends EntitySerializer implements StreamSerializer<MaterializedView> {

	@Override
	public int getTypeId() {
		return DomainSerializationFactory.cli_MaterializedView;
	}

	@Override
	public MaterializedView read(ObjectDataInput in) throws IOException {
		Object[] entity = super.readEntity(in);
		MaterializedView xMatView = new MaterializedView(
				(int) entity[0],
				(Date) entity[1],
				(String) entity[2],
				in.readUTF(),
				in.readInt(),
				in.readUTF(),
				in.readUTF(),
				in.readBoolean());
		int cnt = in.readInt();
		for (int i=0; i < cnt; i++) {
			Parameter xp = in.readObject();
			xMatView.getParameters().add(xp);
		}
		return xMatView;
	}

	@Override
	public void write(ObjectDataOutput out, MaterializedView xMatView) throws IOException {
		super.writeEntity(out, xMatView);
		out.writeUTF(xMatView.getName());
		out.writeInt(xMatView.getCollectionId());
		out.writeUTF(xMatView.getQuery());
		out.writeUTF(xMatView.getDescription());
		out.writeBoolean(xMatView.isEnabled());
		out.writeInt(xMatView.getParameters().size());
		for (Parameter xp: xMatView.getParameters()) {
			out.writeObject(xp);
		}
	}


}

