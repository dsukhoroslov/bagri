package com.bagri.xdm.access.hazelcast.pof;

import java.io.IOException;
import java.util.Properties;

import com.bagri.xdm.system.XDMNode;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.StreamSerializer;

public class XDMNodeSerializer implements StreamSerializer<XDMNode> {

	@Override
	public int getTypeId() {
		return XDMPortableFactory.cli_XDMNode;
	}

	@Override
	public void destroy() {
		// what should we do here?
	}

	@Override
	public XDMNode read(ObjectDataInput in) throws IOException {
		
		XDMNode xNode = new XDMNode(in.readUTF(), 
				in.readUTF(),
				(Properties) in.readObject()); 
		return xNode;
	}

	@Override
	public void write(ObjectDataOutput out, XDMNode xNode)	throws IOException {
		
		out.writeUTF(xNode.getAddress());
		out.writeUTF(xNode.getId());
		out.writeObject(xNode.getOptions());
	}


}
