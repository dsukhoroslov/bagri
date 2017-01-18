package com.bagri.server.hazelcast.store.system;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.bagri.core.system.Node;
import com.hazelcast.core.MapStore;

public class NodeCacheStore extends ConfigCacheStore<String, Node> implements MapStore<String, Node> {

	@SuppressWarnings("unchecked")
	@Override
	protected Map<String, Node> loadEntities() {
		Map<String, Node> nodes = new HashMap<String, Node>();
		Collection<Node> cNodes = (Collection<Node>) cfg.getEntities(Node.class); 
		for (Node node: cNodes) {
			nodes.put(node.getName(), node);
	    }
		return nodes;
	}

	@Override
	protected void storeEntities(Map<String, Node> entities) {
		cfg.setEntities(Node.class, entities.values());
	}


}
