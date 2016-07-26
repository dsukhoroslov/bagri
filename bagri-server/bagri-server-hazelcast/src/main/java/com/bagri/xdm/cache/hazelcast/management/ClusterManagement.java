package com.bagri.xdm.cache.hazelcast.management;

import static com.bagri.xdm.common.Constants.xdm_cluster_node_name;

import java.io.IOException;
import java.util.Properties;

import javax.management.openmbean.TabularData;

import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;

import com.bagri.common.util.PropUtils;
import com.bagri.xdm.cache.hazelcast.task.node.NodeCreator;
import com.bagri.xdm.cache.hazelcast.task.node.NodeRemover;
import com.bagri.xdm.system.Node;
import com.hazelcast.core.HazelcastInstance;

@ManagedResource(objectName="com.bagri.xdm:type=Management,name=ClusterManagement", 
	description="Cluster Management MBean")
public class ClusterManagement extends EntityManagement<Node> {
	
	//, XDMClusterManagement {

	public ClusterManagement(HazelcastInstance hzInstance) {
		super(hzInstance);
	}

	@ManagedAttribute(description="The Nodes Attribute")
	public String[] getNodeNames() {
		return getEntityNames();
	}

	@ManagedAttribute(description="Return registered Nodes")
	public TabularData getNodes() {
		return getEntities("node", "Node definition");
    }
	
	private boolean addNode(String name, Properties options) throws Exception {
	
		if (!entityCache.containsKey(name)) {
			Object result = entityCache.executeOnKey(name, new NodeCreator(getCurrentUser(), name, options));
	    	logger.debug("addNode; execution result: {}", result);
			return true;
		}
		return false;
	}
	
	@ManagedOperation(description="Add new Node")
	@ManagedOperationParameters({
		@ManagedOperationParameter(name = "name", description = "Node name"),
		@ManagedOperationParameter(name = "options", description = "Node options: key/value pairs separated by comma")})
	public boolean addNode(String name, String options) {

		Properties opts;
		try {
			opts = PropUtils.propsFromString(options);
			opts.setProperty(xdm_cluster_node_name, name);
		} catch (IOException ex) {
			logger.error("createSchema.error: ", ex);
			return false;
		}

		try {
			return addNode(name, opts);
		} catch (Exception ex) {
			logger.error("addNode.error: " + ex.getMessage(), ex);
		}
		return false;
	}
	
	@ManagedOperation(description="Delete existing Node")
	@ManagedOperationParameters({
		@ManagedOperationParameter(name = "name", description = "Node name")})
	public boolean deleteNode(String name) {
		Node node = entityCache.get(name);
		if (node != null) {
	    	Object result = entityCache.executeOnKey(name, new NodeRemover(node.getVersion(), getCurrentUser()));
	    	logger.debug("deleteNode; execution result: {}", result);
	    	return result != null;
		}
		return false;
	}

	public NodeManager getNodeManager(String nodeName) {
		logger.trace("getNodeManager.enter; got nodeId: {}", nodeName);
		EntityManager<Node> mgr = mgrCache.get(nodeName); 
		return (NodeManager) mgr;
	}

	@Override
	protected EntityManager<Node> createEntityManager(String nodeName) {
		NodeManager mgr = new NodeManager(hzInstance, nodeName);
		mgr.setEntityCache(entityCache);
		return mgr;
	}

}
