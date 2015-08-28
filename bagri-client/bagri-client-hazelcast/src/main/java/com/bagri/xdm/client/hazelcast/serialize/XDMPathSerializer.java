package com.bagri.xdm.client.hazelcast.serialize;

import java.io.IOException;

import com.bagri.xdm.domain.XDMOccurence;
import com.bagri.xdm.domain.XDMNodeKind;
import com.bagri.xdm.domain.XDMPath;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.StreamSerializer;

public class XDMPathSerializer implements StreamSerializer<XDMPath> {

	@Override
	public int getTypeId() {
		return XDMDataSerializationFactory.cli_XDMPath;
	}

	@Override
	public void destroy() {
	}

	@Override
	public XDMPath read(ObjectDataInput in) throws IOException {
		
		return new XDMPath(
				in.readUTF(),
				in.readInt(),
				XDMNodeKind.valueOf(in.readUTF()),
				in.readInt(),
				in.readInt(),
				in.readInt(),
				in.readInt(),
				XDMOccurence.getOccurence(
						in.readInt(), 
						in.readInt()));
	}

	@Override
	public void write(ObjectDataOutput out, XDMPath xPath) throws IOException {
		
		out.writeUTF(xPath.getPath());
		out.writeInt(xPath.getTypeId());
		out.writeUTF(xPath.getNodeKind().name());
		out.writeInt(xPath.getPathId());
		out.writeInt(xPath.getParentId());
		out.writeInt(xPath.getPostId());
		out.writeInt(xPath.getDataType());
		out.writeInt(xPath.getCardinality().getLowBound());
		out.writeInt(xPath.getCardinality().getHighBound());
	}

}
