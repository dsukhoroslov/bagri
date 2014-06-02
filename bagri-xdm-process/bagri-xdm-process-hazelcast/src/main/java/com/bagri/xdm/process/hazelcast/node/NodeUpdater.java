package com.bagri.xdm.process.hazelcast.node;

import java.io.IOException;
import java.util.Properties;
import java.util.Map.Entry;

import com.bagri.xdm.system.XDMNode;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;

public class NodeUpdater extends NodeProcessor implements DataSerializable {

	private boolean override;
	private Properties options;
	
	public NodeUpdater() {
		//
	}
	
	public NodeUpdater(int version, String admin, boolean override, Properties options) {
		super(version, admin);
		this.override = override;
		this.options = options;
	}

	@Override
	public Object process(Entry<String, XDMNode> entry) {
		logger.debug("process.enter; entry: {}", entry); 
		if (entry.getValue() != null) {
			XDMNode node = entry.getValue();
			if (node.getVersion() == getVersion()) {
				// what if new options are not consistent with the current node state??

				if (override) {
					node.setOptions(options);
				} else {
					for (String name: options.stringPropertyNames()) {
						node.setOption(name, options.getProperty(name));
					}
				}

				if (updateNodeInCluster(node) == 0) {
					logger.info("process; no members updated!"); 
					// rollback changes somehow?
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
		override = in.readBoolean();
		options = in.readObject();
	}

	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		super.writeData(out);
		out.writeBoolean(override);
		out.writeObject(options);
	}

}
