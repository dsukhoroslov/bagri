package com.bagri.client.hazelcast.serialize.model;

import java.io.IOException;

import com.bagri.client.hazelcast.serialize.DomainSerializationFactory;
import com.bagri.core.model.NodeKind;
import com.bagri.core.model.Occurrence;
import com.bagri.core.model.Path;
import com.bagri.support.pool.ContentDataPool;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.StreamSerializer;

public class PathSerializer implements StreamSerializer<Path> {

	@Override
	public int getTypeId() {
		return DomainSerializationFactory.cli_XDMPath;
	}

	@Override
	public void destroy() {
	}

	@Override
	public Path read(ObjectDataInput in) throws IOException {
		ContentDataPool cdPool = ContentDataPool.getDataPool();
		return new Path(
				cdPool.intern(in.readUTF()),
				cdPool.intern(in.readUTF()),
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
