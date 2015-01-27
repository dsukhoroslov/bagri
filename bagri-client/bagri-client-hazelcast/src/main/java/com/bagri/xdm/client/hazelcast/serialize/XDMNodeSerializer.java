package com.bagri.xdm.client.hazelcast.serialize;

import java.io.IOException;
import java.util.Date;
import java.util.Properties;

import com.bagri.xdm.system.XDMNode;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.StreamSerializer;

public class XDMNodeSerializer extends XDMEntitySerializer implements StreamSerializer<XDMNode> {

	@Override
	public int getTypeId() {
		return XDMPortableFactory.cli_XDMNode;
	}

	@Override
	public XDMNode read(ObjectDataInput in) throws IOException {
		Object[] entity = super.readEntity(in);
		XDMNode xNode = new XDMNode(
				(int) entity[0], 
				(Date) entity[1], 
				(String) entity[2], 
				in.readUTF(), 
				(Properties) in.readObject());
		return xNode;
	}

	@Override
	public void write(ObjectDataOutput out, XDMNode xNode) throws IOException {
		super.writeEntity(out, xNode);
		out.writeUTF(xNode.getName());
		out.writeObject(xNode.getOptions());
	}


}
