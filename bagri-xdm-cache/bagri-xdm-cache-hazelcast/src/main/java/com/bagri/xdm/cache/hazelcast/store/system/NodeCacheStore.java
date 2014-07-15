package com.bagri.xdm.cache.hazelcast.store.system;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.bagri.xdm.system.XDMNode;
import com.hazelcast.core.MapStore;

public class NodeCacheStore extends ConfigCacheStore<String, XDMNode> implements MapStore<String, XDMNode> {

	@SuppressWarnings("unchecked")
	@Override
	protected Map<String, XDMNode> loadEntities() {
		Map<String, XDMNode> nodes = new HashMap<String, XDMNode>();
		Collection<XDMNode> cNodes = (Collection<XDMNode>) cfg.getEntities(XDMNode.class); 
		for (XDMNode node: cNodes) {
			nodes.put(node.getName(), node);
	    }
		return nodes;
	}

	@Override
	protected void storeEntities(Map<String, XDMNode> entities) {
		cfg.setEntities(XDMNode.class, entities.values());
	}


}
