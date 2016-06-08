package com.bagri.xdm.cache.hazelcast.task.node;

import static com.bagri.xdm.cache.hazelcast.serialize.DataSerializationFactoryImpl.cli_UpdateNodeTask;

import java.io.IOException;
import java.util.Properties;
import java.util.Map.Entry;

import com.bagri.xdm.system.XDMNode;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;

public class NodeUpdater extends NodeProcessor implements IdentifiedDataSerializable {

	private String comment;
	private boolean override;
	private Properties options;
	
	public NodeUpdater() {
		//
	}
	
	public NodeUpdater(int version, String admin, String comment, boolean override, Properties options) {
		super(version, admin);
		this.comment = comment;
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

				if (updateNodesInCluster(node, comment) == 0) {
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
	public int getId() {
		return cli_UpdateNodeTask;
	}
	
	@Override
	public void readData(ObjectDataInput in) throws IOException {
		super.readData(in);
		comment = in.readUTF();
		override = in.readBoolean();
		options = in.readObject();
	}

	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		super.writeData(out);
		out.writeUTF(comment);
		out.writeBoolean(override);
		out.writeObject(options);
	}

}
