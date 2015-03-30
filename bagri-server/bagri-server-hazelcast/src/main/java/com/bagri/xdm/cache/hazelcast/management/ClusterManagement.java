package com.bagri.xdm.cache.hazelcast.management;

import java.io.IOException;
import java.util.Properties;
import java.util.Set;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;

import com.bagri.common.config.XDMConfigConstants;
import com.bagri.common.manage.JMXUtils;
import com.bagri.common.util.FileUtils;
import com.bagri.common.util.PropUtils;
import com.bagri.xdm.cache.hazelcast.task.node.NodeCreator;
import com.bagri.xdm.cache.hazelcast.task.node.NodeRemover;
import com.bagri.xdm.system.XDMNode;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.MapEvent;

@ManagedResource(objectName="com.bagri.xdm:type=Management,name=ClusterManagement", 
	description="Cluster Management MBean")
public class ClusterManagement extends EntityManagement<String, XDMNode> {
	
	//, XDMClusterManagement {

	public ClusterManagement(HazelcastInstance hzInstance) {
		super(hzInstance);
	}

	private boolean addNode(String name, Properties options) throws Exception {
	
		if (!entityCache.containsKey(name)) {
	    	Object result = entityCache.executeOnKey(name, 
	    			new NodeCreator(JMXUtils.getCurrentUser(), name, options));
	    	logger.debug("addNode; execution result: {}", result);
			return true;
		}
		return false;
	}
	
	@ManagedAttribute(description="The Nodes Attribute")
	public String[] getNodes() {
		return entityCache.keySet().toArray(new String[0]);
	}

	@ManagedOperation(description="Add new Node")
	@ManagedOperationParameters({
		@ManagedOperationParameter(name = "name", description = "Node name"),
		@ManagedOperationParameter(name = "options", description = "Node options: key/value pairs separated by comma")})
	public boolean addNode(String name, String options) {

		Properties opts;
		try {
			opts = PropUtils.propsFromString(options);
			opts.setProperty(XDMConfigConstants.xdm_cluster_node_name, name);
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
		XDMNode node = entityCache.get(name);
		if (node != null) {
	    	Object result = entityCache.executeOnKey(name, new NodeRemover(node.getVersion(), JMXUtils.getCurrentUser()));
	    	logger.debug("deleteNode; execution result: {}", result);
	    	return result != null;
		}
		return false;
	}

	public NodeManager getNodeManager(String nodeName) {
		logger.trace("getNodeManager.enter; got nodeId: {}", nodeName);
		EntityManager<XDMNode> mgr = mgrCache.get(nodeName); 
		return (NodeManager) mgr;
	}

	@Override
	protected EntityManager<XDMNode> createEntityManager(String nodeName) {
		NodeManager mgr = new NodeManager(hzInstance, nodeName);
		mgr.setEntityCache(entityCache);
		return mgr;
	}

}
