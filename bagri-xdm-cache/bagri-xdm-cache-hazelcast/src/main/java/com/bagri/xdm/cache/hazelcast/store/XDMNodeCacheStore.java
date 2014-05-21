package com.bagri.xdm.cache.hazelcast.store;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.bagri.xdm.system.XDMNode;
import com.hazelcast.core.MapStore;

public class XDMNodeCacheStore extends ConfigCacheStore<String, XDMNode> implements MapStore<String, XDMNode> {

	@Override
	protected Map<String, XDMNode> loadEntities() {
		Map<String, XDMNode> nodes = new HashMap<String, XDMNode>();
		for (XDMNode node: cfg.getNodes()) {
			nodes.put(node.getId(), node);
	    }
		return nodes;
	}

	@Override
	protected void storeEntities(Map<String, XDMNode> entities) {
		cfg.setNodes(entities.values());
	}


}
