package com.bagri.xdm.process.hazelcast.node;

import java.util.Map.Entry;

import com.bagri.xdm.system.XDMNode;
import com.hazelcast.nio.serialization.DataSerializable;

public class NodeRemover extends NodeProcessor implements DataSerializable {

	public NodeRemover() {
		//
	}
	
	public NodeRemover(int version, String admin) {
		super(version, admin);
	}

	@Override
	public Object process(Entry<String, XDMNode> entry) {
		logger.debug("process.enter; entry: {}", entry); 
		if (entry.getValue() != null) {
			XDMNode node = entry.getValue();
			if (node.getVersion() == getVersion()) {
				entry.setValue(null);
				auditEntity(AuditType.delete, node);
				return node;
			} else {
				// throw ex ?
				logger.warn("process; outdated user version: {}; entry version: {}; process terminated", 
						getVersion(), entry.getValue().getVersion()); 
			}
		} 
		return null;
	}	
	
}
