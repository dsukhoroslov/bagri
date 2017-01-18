package com.bagri.server.hazelcast.serialize.system;

import java.io.IOException;
import java.util.Date;
import java.util.Properties;

import com.bagri.client.hazelcast.serialize.DataSerializationFactoryImpl;
import com.bagri.core.system.Node;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.StreamSerializer;

public class NodeSerializer extends EntitySerializer implements StreamSerializer<Node> {

	@Override
	public int getTypeId() {
		return DataSerializationFactoryImpl.cli_XDMNode;
	}

	@Override
	public Node read(ObjectDataInput in) throws IOException {
		Object[] entity = super.readEntity(in);
		Node xNode = new Node(
				(int) entity[0], 
				(Date) entity[1], 
				(String) entity[2], 
				in.readUTF(), 
				(Properties) in.readObject());
		return xNode;
	}

	@Override
	public void write(ObjectDataOutput out, Node xNode) throws IOException {
		super.writeEntity(out, xNode);
		out.writeUTF(xNode.getName());
		out.writeObject(xNode.getOptions());
	}


}
