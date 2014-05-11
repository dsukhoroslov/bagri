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
import com.bagri.xdm.access.api.XDMClusterManagement;
import com.bagri.xdm.access.api.XDMNodeManager;
import com.bagri.xdm.process.hazelcast.node.NodeCreator;
import com.bagri.xdm.process.hazelcast.node.NodeRemover;
import com.bagri.xdm.process.hazelcast.user.UserCreator;
import com.bagri.xdm.process.hazelcast.user.UserRemover;
import com.bagri.xdm.system.XDMNode;
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
public class ClusterManagement implements EntryListener<String, XDMNode>, InitializingBean,	
	MembershipListener, XDMClusterManagement {

    private static final transient Logger logger = LoggerFactory.getLogger(ClusterManagement.class);
	//private static final String cluster_management = "ClusterManagement";
    
    private HazelcastInstance hzInstance;
    private IMap<String, XDMNode> nodeCache;
    private Map<String, NodeManager> mgrCache = new HashMap<String, NodeManager>();

    @Autowired
	private AnnotationMBeanExporter mbeanExporter;
    
	public ClusterManagement(HazelcastInstance hzInstance) {
		//super();
		this.hzInstance = hzInstance;
		hzInstance.getCluster().addMembershipListener(this);
	}

	@Override
	public void afterPropertiesSet() throws Exception {
        Set<String> names = nodeCache.keySet();
        for (String name: names) {
        	//XDMNode node = nodeCache.get(name);
        	initNodeManager(name);
        }
		
		// skip it and wait till we get attribute change event
		Set<Member> members = hzInstance.getCluster().getMembers();
		logger.debug("afterPropertiesSet.enter; initiating {} nodes", members.size());
		for (Member member: members) {
			initNode(member);
		}
	}
	
	private void initNode(Member member) throws Exception {
		Properties opts = new Properties();
		opts.putAll(member.getAttributes());
		String address = member.getSocketAddress().getHostString();
		String id = member.getUuid();
		addNode(id, address, opts);
	}
	
	private boolean addNode(String id, String address, Properties options) throws Exception {
	
		String key = getNodeKey(address, id);
		if (!nodeCache.containsKey(key)) {
	    	Object result = nodeCache.executeOnKey(key, new NodeCreator(JMXUtils.getCurrentUser(), 
	    			id, address, options));
	    	logger.debug("addUser; execution result: {}", result);
			return true;
		}
		return false;
		
	}
	
	private boolean denitNode(XDMNode node) throws Exception {
		// TODO: deactivate node!
		return nodeCache.remove(node.getNode()) != null;
	}
	
	public void setNodeCache(IMap<String, XDMNode> nodeCache) {
		this.nodeCache = nodeCache;
		nodeCache.addEntryListener(this, false);
	}

	@ManagedAttribute(description="The Nodes Attribute")
	public String[] getNodes() {
		return nodeCache.keySet().toArray(new String[0]);
	}

	@ManagedOperation(description="Add new Node")
	@ManagedOperationParameters({
		@ManagedOperationParameter(name = "address", description = "Node address"),
		@ManagedOperationParameter(name = "nodeId", description = "Node identifier"),
		@ManagedOperationParameter(name = "options", description = "Node options: key/value pairs separated by comma")})
	public boolean addNode(String address, String nodeId, String options) {
		Properties opts = new Properties();
		options = options.replaceAll(";", "\n\r");
		try {
			opts.load(new StringReader(options));
		} catch (IOException ex) {
			logger.error("createSchema.error: ", ex);
			return false;
		}
		
		try {
			return addNode(nodeId, address, options);
		} catch (Exception ex) {
			logger.error("addNode.error: " + ex.getMessage(), ex);
		}
		return false;
	}
	
	private String getNodeKey(String address, String id) {
		return id + "[" + address + "]";
	}

	@ManagedOperation(description="Delete existing Node")
	@ManagedOperationParameters({
		@ManagedOperationParameter(name = "address", description = "Node address"),
		@ManagedOperationParameter(name = "nodeId", description = "Node identifier")})
	public boolean deleteNode(String address, String nodeId) {
		//String key = getNodeKey(address, nodeId);
		//XDMNode node = nodeCache.remove(key);
		//return node != null;
		
		String key = getNodeKey(address, nodeId);
		XDMNode node = nodeCache.get(key);
		if (node != null) {
	    	Object result = nodeCache.executeOnKey(key, new NodeRemover(node.getVersion(), JMXUtils.getCurrentUser()));
	    	logger.debug("deleteNode; execution result: {}", result);
	    	return result != null;
		}
		return false;
	}

	@Override
	public void memberAdded(MembershipEvent membershipEvent) {
		try {
			initNode(membershipEvent.getMember());
		} catch (Exception ex) {
			logger.error("memberAdded.error: " + ex.getMessage(), ex);
		}
	}

	@Override
	public void memberRemoved(MembershipEvent membershipEvent) {
		String address = membershipEvent.getMember().getSocketAddress().getHostString();
		String id = membershipEvent.getMember().getUuid();
		// TODO: denitNode!?
		deleteNode(address, id);
	}

	@Override
	public void memberAttributeChanged(MemberAttributeEvent memberAttributeEvent) {
		// change node options ?
		logger.debug("memberAttributeChanged.enter; value: {}", memberAttributeEvent.getValue());

		Member member = memberAttributeEvent.getMember();
		String address = member.getSocketAddress().getHostString();
		String id = member.getUuid();
		String key = getNodeKey(address, id);
		NodeManager nMgr = (NodeManager) getNodeManager(key);
		if (nMgr != null) {
			nMgr.setNodeOption(memberAttributeEvent.getKey(), memberAttributeEvent.getValue().toString());
		}
	}

	@Override
	public XDMNodeManager getNodeManager(String nodeId) {
		logger.trace("getNodeManager.enter; got nodeId: {}", nodeId);
		return mgrCache.get(nodeId);
	}

	private NodeManager initNodeManager(String nodeName) throws MBeanExportException, MalformedObjectNameException {
		NodeManager nMgr = null;
   	    if (!mgrCache.containsKey(nodeName)) {
			nMgr = new NodeManager(hzInstance, nodeName);
			//nMgr.setNodeCache(nodeCache);
			mgrCache.put(nodeName, nMgr);
			mbeanExporter.registerManagedResource(nMgr, nMgr.getObjectName());
		}
   	    return nMgr;
	}

	
	@Override
	public void entryAdded(EntryEvent<String, XDMNode> event) {
		String nodeName = event.getKey();
		try {
			initNodeManager(nodeName);
		} catch (MBeanExportException | MalformedObjectNameException ex) {
			// JMX registration failed.
			logger.error("entryAdded.error: ", ex);
		}
	}

	@Override
	public void entryRemoved(EntryEvent<String, XDMNode> event) {
		String nodeName = event.getKey();
		if (mgrCache.containsKey(nodeName)) {
			NodeManager nMgr = mgrCache.get(nodeName);
			mgrCache.remove(nodeName);
			try {
				mbeanExporter.unregisterManagedResource(nMgr.getObjectName());
			} catch (MalformedObjectNameException ex) {
				logger.error("entryRemoved.error: ", ex);
			}
		}
	}

	@Override
	public void entryUpdated(EntryEvent<String, XDMNode> event) {
		logger.trace("entryUpdated; event: {}", event);
	}

	@Override
	public void entryEvicted(EntryEvent<String, XDMNode> event) {
		logger.trace("entryEvicted; event: {}", event);
		// make node inactive ?
	}

	
}
