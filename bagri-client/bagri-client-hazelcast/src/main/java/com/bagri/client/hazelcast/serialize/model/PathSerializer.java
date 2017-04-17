package com.bagri.client.hazelcast.serialize.model;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.client.hazelcast.serialize.DataSerializationFactoryImpl;
import com.bagri.core.model.NodeKind;
import com.bagri.core.model.Occurrence;
import com.bagri.core.model.Path;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.StreamSerializer;

public class PathSerializer implements StreamSerializer<Path> {

	private static final transient Logger logger = LoggerFactory.getLogger(PathSerializer.class);
	
	@Override
	public int getTypeId() {
		return DataSerializationFactoryImpl.cli_XDMPath;
	}

	@Override
	public void destroy() {
	}

	@Override
	public Path read(ObjectDataInput in) throws IOException {
		logger.trace("read;");
		return new Path(
				in.readUTF(),
				in.readUTF(),
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
		logger.trace("write;");
		out.writeUTF(xPath.getPath());
		out.writeUTF(xPath.getRoot());
		out.writeInt(xPath.getNodeKind().ordinal());
		out.writeInt(xPath.getPathId());
		out.writeInt(xPath.getParentId());
		out.writeInt(xPath.getPostId());
		out.writeInt(xPath.getDataType());
		out.writeInt(xPath.getOccurrence().getLowBound());
		out.writeInt(xPath.getOccurrence().getHighBound());
	}

}
