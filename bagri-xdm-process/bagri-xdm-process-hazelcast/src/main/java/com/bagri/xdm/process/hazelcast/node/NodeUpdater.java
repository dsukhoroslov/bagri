package com.bagri.xdm.process.hazelcast.node;

import java.io.IOException;
import java.util.Properties;
import java.util.Map.Entry;

import com.bagri.xdm.system.XDMNode;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;

public class NodeUpdater extends NodeProcessor implements DataSerializable {

	private Properties options;
	
	public NodeUpdater() {
		//
	}
	
	public NodeUpdater(int version, String admin, Properties options) {
		super(version, admin);
		this.options = options;
	}

	@Override
	public Object process(Entry<String, XDMNode> entry) {
		logger.debug("process.enter; entry: {}", entry); 
		if (entry.getValue() != null) {
			XDMNode node = entry.getValue();
			if (node.getVersion() == getVersion()) {
				// what if new options are not consistent with the current node state??
				
				for (String name: options.stringPropertyNames()) {
					node.setOption(name, options.getProperty(name));
				}
				
				node.updateVersion(getAdmin());
				entry.setValue(node);
				auditEntity(AuditType.update, node);
				return node;
			}
		} 
		return null;
	}

	@Override
	public void readData(ObjectDataInput in) throws IOException {
		super.readData(in);
		options = in.readObject();
	}

	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		super.writeData(out);
		out.writeObject(options);
	}

}
