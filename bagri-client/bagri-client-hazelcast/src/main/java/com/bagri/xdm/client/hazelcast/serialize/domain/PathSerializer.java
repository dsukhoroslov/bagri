package com.bagri.xdm.client.hazelcast.serialize.domain;

import java.io.IOException;

import com.bagri.xdm.domain.Occurrence;
import com.bagri.xdm.client.hazelcast.serialize.DataSerializationFactoryImpl;
import com.bagri.xdm.domain.NodeKind;
import com.bagri.xdm.domain.Path;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.StreamSerializer;

public class PathSerializer implements StreamSerializer<Path> {

	@Override
	public int getTypeId() {
		return DataSerializationFactoryImpl.cli_XDMPath;
	}

	@Override
	public void destroy() {
	}

	@Override
	public Path read(ObjectDataInput in) throws IOException {
		
		return new Path(
				in.readUTF(),
				in.readInt(),
				NodeKind.values()[in.readInt()],
				in.readInt(),
				in.readInt(),
				in.readInt(),
				in.readInt(),
				Occurrence.getOccurrence(
						in.readInt(), 
						in.readInt()));
	}

	@Override
	public void write(ObjectDataOutput out, Path xPath) throws IOException {
		
		out.writeUTF(xPath.getPath());
		out.writeInt(xPath.getTypeId());
		out.writeInt(xPath.getNodeKind().ordinal());
		out.writeInt(xPath.getPathId());
		out.writeInt(xPath.getParentId());
		out.writeInt(xPath.getPostId());
		out.writeInt(xPath.getDataType());
		out.writeInt(xPath.getOccurrence().getLowBound());
		out.writeInt(xPath.getOccurrence().getHighBound());
	}

}
