package com.bagri.server.hazelcast.serialize.system;

import java.io.IOException;
import java.util.Date;

import com.bagri.client.hazelcast.serialize.DataSerializationFactoryImpl;
import com.bagri.core.system.Function;
import com.bagri.core.system.Library;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.StreamSerializer;

public class LibrarySerializer extends EntitySerializer implements StreamSerializer<Library> {

	@Override
	public int getTypeId() {
		return DataSerializationFactoryImpl.cli_XDMLibrary;
	}

	@Override
	public Library read(ObjectDataInput in) throws IOException {
		Object[] entity = super.readEntity(in);
		Library xLibrary = new Library(
				(int) entity[0],
				(Date) entity[1],
				(String) entity[2],
				in.readUTF(),
				in.readUTF(),
				in.readUTF(),
				in.readBoolean());
		int cnt = in.readInt();
		for (int i=0; i < cnt; i++) {
			Function xf = in.readObject();
			xLibrary.getFunctions().add(xf);
		}
		return xLibrary;
	}

	@Override
	public void write(ObjectDataOutput out, Library xLibrary) throws IOException {
		super.writeEntity(out, xLibrary);
		out.writeUTF(xLibrary.getName());
		out.writeUTF(xLibrary.getFileName());
		out.writeUTF(xLibrary.getDescription());
		out.writeBoolean(xLibrary.isEnabled());
		out.writeInt(xLibrary.getFunctions().size());
		for (Function xf: xLibrary.getFunctions()) {
			out.writeObject(xf);
		}
	}

}
