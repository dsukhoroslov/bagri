package com.bagri.xdm.cache.hazelcast.management;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import com.bagri.common.manage.JMXUtils;
import com.bagri.xdm.XDMNode;
import com.bagri.xdm.access.api.XDMClusterManagement;
import com.bagri.xdm.access.api.XDMNodeManager;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.Member;
import com.hazelcast.core.MemberAttributeEvent;
import com.hazelcast.core.MembershipEvent;
import com.hazelcast.core.MembershipListener;

public class ClusterManagement implements InitializingBean, MembershipListener, 
	ClusterManagementMBean, XDMClusterManagement {

    private static final transient Logger logger = LoggerFactory.getLogger(ClusterManagement.class);
	private static final String cluster_management = "ClusterManagement";
	private static final String type_management = "Management";
    
    private HazelcastInstance hzInstance;
    private IMap<String, XDMNode> nodeCache;
    private Map<String, NodeManager> mgrCache = new HashMap<String, NodeManager>();
    
	public ClusterManagement(HazelcastInstance hzInstance) {
		//super();
		this.hzInstance = hzInstance;
		hzInstance.getCluster().addMembershipListener(this);
	}

	@Override
	public void afterPropertiesSet() throws Exception {
        //Set<String> names = nodeCache.keySet();
        //for (String name: names) {
        //	XDMNode node = nodeCache.get(name);
        //	initNode(node);
        //}
		
		Set<Member> members = hzInstance.getCluster().getMembers();
		logger.debug("afterPropertiesSet.enter; initiating {} nodes", members.size());
		for (Member member: members) {
			initNode(member);
		}
		
		JMXUtils.registerMBean(type_management, cluster_management, this);
	}
	
	private void initNode(Member member) throws Exception {
		Properties opts = new Properties();
		opts.putAll(member.getAttributes());
		String address = member.getSocketAddress().getHostString();
		String id = member.getUuid();
		XDMNode node = new XDMNode(address, id, opts);
		initNode(node);
	}
	
	private boolean initNode(XDMNode node) throws Exception {
		if (!nodeCache.containsKey(node.getNode())) {
			nodeCache.put(node.getNode(), node);
		}

		if (!mgrCache.containsKey(node.getNode())) {
			NodeManager nMgr = new NodeManager(hzInstance, node.getNode()); 
			mgrCache.put(node.getNode(), nMgr);
			nMgr.afterPropertiesSet();
			return true;
		}
		return false;
	}
	
	private boolean denitNode(XDMNode node) {
		// find and unreg NodeManager...
		NodeManager nMgr = mgrCache.get(node.getNode());
		if (nMgr != null) {
			nMgr.close();
			return true;
		}
		return false;
	}
	
	public void setNodeCache(IMap<String, XDMNode> nodeCache) {
		this.nodeCache = nodeCache;
	}

	@Override
	public String[] getNodes() {
		return nodeCache.keySet().toArray(new String[0]);
	}

	@Override
	public boolean addNode(String address, String nodeId, String options) {
		Properties opts = new Properties();
		options = options.replaceAll(";", "\n\r");
		try {
			opts.load(new StringReader(options));
		} catch (IOException ex) {
			logger.error("createSchema.error: ", ex);
			return false;
		}
		XDMNode node = new XDMNode(address, nodeId, opts);
		
		try {
			return initNode(node);
		} catch (Exception ex) {
			logger.error("addNode.error: " + ex.getMessage(), ex);
		}
		return false;
	}
	
	private String getNodeKey(String address, String id) {
		return id + "[" + address + "]";
	}

	@Override
	public boolean deleteNode(String address, String nodeId) {
		String key = getNodeKey(address, nodeId);
		XDMNode node = nodeCache.remove(key);
		if (node != null) {
			return denitNode(node);
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
		deleteNode(address, id);
	}

	@Override
	public void memberAttributeChanged(MemberAttributeEvent memberAttributeEvent) {
		// change node options ?
		logger.debug("memberAttributeChanged.enter; value: {}", memberAttributeEvent.getValue());
		
		String address = memberAttributeEvent.getMember().getSocketAddress().getHostString();
		String id = memberAttributeEvent.getMember().getUuid();
		String key = getNodeKey(address, id);
		NodeManager nMgr = mgrCache.get(key);
		if (nMgr != null) {
			nMgr.setNodeOption(memberAttributeEvent.getKey(), memberAttributeEvent.getValue().toString());
		}
	}

	@Override
	public XDMNodeManager getNodeManager(String nodeId) {
		return mgrCache.get(nodeId);
	}

}
