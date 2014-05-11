package com.bagri.xdm.process.hazelcast.node;

import java.io.IOException;
import java.util.Date;
import java.util.Properties;
import java.util.Map.Entry;

import com.bagri.xdm.system.XDMNode;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;

public class NodeCreator extends NodeProcessor implements DataSerializable {

	private String id;
	private String address;
	private Properties options;
	
	public NodeCreator() {
		//
	}
	
	public NodeCreator(String admin, String id, String address, Properties options) {
		super(1, admin);
		this.id = id;
		this.address = address;
		this.options = options;
	}

	@Override
	public Object process(Entry<String, XDMNode> entry) {
		logger.debug("process.enter; entry: {}", entry); 
		if (entry.getValue() == null) {
			String nodeId = entry.getKey();
			XDMNode node = new XDMNode(address, id, options, getVersion(), new Date(), getAdmin());
			entry.setValue(node);
			auditEntity(AuditType.create, node);
			return node;
		} 
		return null;
	}

	@Override
	public void readData(ObjectDataInput in) throws IOException {
		super.readData(in);
		id = in.readUTF();
		address = in.readUTF();
		options = in.readObject();
	}

	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		super.writeData(out);
		out.writeUTF(id);
		out.writeUTF(address);
		out.writeObject(options);
	}

}
