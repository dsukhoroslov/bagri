package com.bagri.xdm.cache.hazelcast.management;

import java.io.IOException;
import java.io.StringReader;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.management.MalformedObjectNameException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jmx.export.MBeanExportException;
import org.springframework.jmx.export.annotation.AnnotationMBeanExporter;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;

import com.bagri.common.manage.JMXUtils;
import com.bagri.common.util.FileUtils;
import com.bagri.xdm.access.api.XDMClusterManagement;
import com.bagri.xdm.access.api.XDMNodeManager;
import com.bagri.xdm.process.hazelcast.node.NodeCreator;
import com.bagri.xdm.process.hazelcast.node.NodeRemover;
import com.bagri.xdm.process.hazelcast.user.UserCreator;
import com.bagri.xdm.process.hazelcast.user.UserRemover;
import com.bagri.xdm.system.XDMNode;
import com.bagri.xdm.system.XDMRole;
import com.bagri.xdm.system.XDMUser;
import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.EntryListener;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.Member;
import com.hazelcast.core.MemberAttributeEvent;
import com.hazelcast.core.MembershipEvent;
import com.hazelcast.core.MembershipListener;

@ManagedResource(objectName="com.bagri.xdm:type=Management,name=ClusterManagement", 
	description="Cluster Management MBean")
public class ClusterManagement extends EntityManagement<String, XDMNode> implements InitializingBean, XDMClusterManagement {

	public ClusterManagement(HazelcastInstance hzInstance) {
		super(hzInstance);
	}

	@Override
	public void afterPropertiesSet() throws Exception {
        Set<String> names = entityCache.keySet();
        for (String name: names) {
        	//XDMNode node = nodeCache.get(name);
        	//initNodeManager(name);
        }
		
		// skip it and wait till we get attribute change event
		//Set<Member> members = hzInstance.getCluster().getMembers();
		//logger.debug("afterPropertiesSet.enter; initiating {} nodes", members.size());
		//for (Member member: members) {
		//	initNode(member);
		//}
	}
	
	private void initNode(Member member) throws Exception {
		Properties opts = new Properties();
		opts.putAll(member.getAttributes());
		String address = member.getSocketAddress().getHostString();
		String id = member.getUuid();
		addNode(id, address, opts);
	}
	
	private boolean addNode(String name, String address, Properties options) throws Exception {
	
		String key = getNodeKey(name, address);
		if (!entityCache.containsKey(key)) {
	    	Object result = entityCache.executeOnKey(key, new NodeCreator(JMXUtils.getCurrentUser(), 
	    			name, address, options));
	    	logger.debug("addNode; execution result: {}", result);
			return true;
		}
		return false;
		
	}
	
	private boolean denitNode(XDMNode node) throws Exception {
		// TODO: deactivate node!
		return entityCache.remove(node.getNode()) != null;
	}
	
	@ManagedAttribute(description="The Nodes Attribute")
	public String[] getNodes() {
		return entityCache.keySet().toArray(new String[0]);
	}

	@ManagedOperation(description="Add new Node")
	@ManagedOperationParameters({
		@ManagedOperationParameter(name = "name", description = "Node name"),
		@ManagedOperationParameter(name = "address", description = "Node address"),
		@ManagedOperationParameter(name = "options", description = "Node options: key/value pairs separated by comma")})
	public boolean addNode(String name, String address, String options) {

		Properties opts;
		try {
			opts = FileUtils.propsFromString(options);
			opts.setProperty(XDMNode.op_node_name, name);
		} catch (IOException ex) {
			logger.error("createSchema.error: ", ex);
			return false;
		}

		try {
			return addNode(name, address, opts);
		} catch (Exception ex) {
			logger.error("addNode.error: " + ex.getMessage(), ex);
		}
		return false;
	}
	
	private String getNodeKey(String name, String address) {
		return name + "[" + address + "]";
	}

	@ManagedOperation(description="Delete existing Node")
	@ManagedOperationParameters({
		@ManagedOperationParameter(name = "name", description = "Node name"),
		@ManagedOperationParameter(name = "address", description = "Node address")})
	public boolean deleteNode(String name, String address) {
		//String key = getNodeKey(address, nodeId);
		//XDMNode node = nodeCache.remove(key);
		//return node != null;
		
		String key = getNodeKey(name, address);
		XDMNode node = entityCache.get(key);
		if (node != null) {
	    	Object result = entityCache.executeOnKey(key, new NodeRemover(node.getVersion(), JMXUtils.getCurrentUser()));
	    	logger.debug("deleteNode; execution result: {}", result);
	    	return result != null;
		}
		return false;
	}

	@Override
	public XDMNodeManager getNodeManager(String nodeId) {
		logger.trace("getNodeManager.enter; got nodeId: {}", nodeId);
		EntityManager<XDMNode> mgr = mgrCache.get(nodeId); 
		return (NodeManager) mgr;
	}

	@Override
	protected EntityManager<XDMNode> createEntityManager(String nodeName) {
		NodeManager mgr = new NodeManager(hzInstance, nodeName);
		mgr.setEntityCache(entityCache);
		return mgr;
	}

}
