package com.bagri.xdm.client.hazelcast.serialize.domain;

import java.io.IOException;

import com.bagri.xdm.domain.XDMOccurrence;
import com.bagri.xdm.client.hazelcast.serialize.DataSerializationFactoryImpl;
import com.bagri.xdm.domain.XDMNodeKind;
import com.bagri.xdm.domain.XDMPath;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.StreamSerializer;

public class XDMPathSerializer implements StreamSerializer<XDMPath> {

	@Override
	public int getTypeId() {
		return DataSerializationFactoryImpl.cli_XDMPath;
	}

	@Override
	public void destroy() {
	}

	@Override
	public XDMPath read(ObjectDataInput in) throws IOException {
		
		return new XDMPath(
				in.readUTF(),
				in.readInt(),
				XDMNodeKind.values()[in.readInt()],
				in.readInt(),
				in.readInt(),
				in.readInt(),
				in.readInt(),
				XDMOccurrence.getOccurrence(
						in.readInt(), 
						in.readInt()));
	}

	@Override
	public void write(ObjectDataOutput out, XDMPath xPath) throws IOException {
		
		out.writeUTF(xPath.getPath());
		out.writeInt(xPath.getTypeId());
		out.writeInt(xPath.getNodeKind().ordinal());
		out.writeInt(xPath.getPathId());
		out.writeInt(xPath.getParentId());
		out.writeInt(xPath.getPostId());
		out.writeInt(xPath.getDataType());
		out.writeInt(xPath.getOccurence().getLowBound());
		out.writeInt(xPath.getOccurence().getHighBound());
	}

}
