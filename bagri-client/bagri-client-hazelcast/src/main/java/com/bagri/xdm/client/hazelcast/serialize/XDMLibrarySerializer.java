package com.bagri.xdm.client.hazelcast.serialize;

import java.io.IOException;
import java.util.Date;

import com.bagri.xdm.system.XDMFunction;
import com.bagri.xdm.system.XDMLibrary;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.StreamSerializer;

public class XDMLibrarySerializer extends XDMEntitySerializer implements StreamSerializer<XDMLibrary> {

	@Override
	public int getTypeId() {
		return XDMDataSerializationFactory.cli_XDMLibrary;
	}

	@Override
	public XDMLibrary read(ObjectDataInput in) throws IOException {
		Object[] entity = super.readEntity(in);
		XDMLibrary xLibrary = new XDMLibrary(
				(int) entity[0],
				(Date) entity[1],
				(String) entity[2],
				in.readUTF(),
				in.readUTF(),
				in.readUTF(),
				in.readBoolean());
		int cnt = in.readInt();
		for (int i=0; i < cnt; i++) {
			XDMFunction xf = in.readObject();
			xLibrary.getFunctions().add(xf);
		}
		return xLibrary;
	}

	@Override
	public void write(ObjectDataOutput out, XDMLibrary xLibrary) throws IOException {
		super.writeEntity(out, xLibrary);
		out.writeUTF(xLibrary.getName());
		out.writeUTF(xLibrary.getFileName());
		out.writeUTF(xLibrary.getDescription());
		out.writeBoolean(xLibrary.isEnabled());
		out.writeInt(xLibrary.getFunctions().size());
		for (XDMFunction xf: xLibrary.getFunctions()) {
			out.writeObject(xf);
		}
	}

}
