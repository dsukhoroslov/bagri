package com.bagri.server.hazelcast.task.node;

import static com.bagri.server.hazelcast.serialize.DataSerializationFactoryImpl.cli_CreateNodeTask;

import java.io.IOException;
import java.util.Date;
import java.util.Properties;
import java.util.Map.Entry;

import com.bagri.core.system.Node;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;

public class NodeCreator extends NodeProcessor implements IdentifiedDataSerializable {

	private String name;
	private Properties options;
	
	public NodeCreator() {
		//
	}
	
	public NodeCreator(String admin, String name, Properties options) {
		super(1, admin);
		this.name = name;
		this.options = options;
	}

	@Override
	public Object process(Entry<String, Node> entry) {
		logger.debug("process.enter; entry: {}", entry); 
		if (entry.getValue() == null) {
			String nodeId = entry.getKey();
			Node node = new Node(getVersion(), new Date(), getAdmin(), name, options);
			entry.setValue(node);
			auditEntity(AuditType.create, node);
			return node;
		} 
		return null;
	}

	@Override
	public int getId() {
		return cli_CreateNodeTask;
	}
	
	@Override
	public void readData(ObjectDataInput in) throws IOException {
		super.readData(in);
		name = in.readUTF();
		options = in.readObject();
	}

	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		super.writeData(out);
		out.writeUTF(name);
		out.writeObject(options);
	}

}
