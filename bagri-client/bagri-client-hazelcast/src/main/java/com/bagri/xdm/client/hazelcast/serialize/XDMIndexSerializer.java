package com.bagri.xdm.client.hazelcast.serialize;

import java.io.IOException;
import java.util.Date;

import javax.xml.namespace.QName;

import com.bagri.xdm.system.XDMIndex;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.StreamSerializer;

public class XDMIndexSerializer extends XDMEntitySerializer implements StreamSerializer<XDMIndex> {

	@Override
	public int getTypeId() {
		return DataSerializationFactoryImpl.cli_XDMIndex;
	}

	@Override
	public XDMIndex read(ObjectDataInput in) throws IOException {
		Object[] entity = super.readEntity(in);
		XDMIndex xIndex = new XDMIndex(
				(int) entity[0],
				(Date) entity[1],
				(String) entity[2],
				in.readUTF(),
				in.readUTF(),
				in.readUTF(),
				in.readUTF(),
				QName.valueOf(in.readUTF()),
				in.readBoolean(),
				in.readBoolean(),
				in.readBoolean(),
				in.readUTF(),
				in.readBoolean());
		return xIndex;
	}

	@Override
	public void write(ObjectDataOutput out, XDMIndex xIndex) throws IOException {
		
		super.writeEntity(out, xIndex);
		out.writeUTF(xIndex.getName());
		out.writeUTF(xIndex.getDocumentType());
		out.writeUTF(xIndex.getTypePath());
		out.writeUTF(xIndex.getPath());
		out.writeUTF(xIndex.getDataType().toString());
		out.writeBoolean(xIndex.isCaseSensitive());
		out.writeBoolean(xIndex.isRange());
		out.writeBoolean(xIndex.isUnique());
		out.writeUTF(xIndex.getDescription());
		out.writeBoolean(xIndex.isEnabled());
	}


}
