package com.bagri.xdm.cache.hazelcast.management;

import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.management.openmbean.CompositeData;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import com.bagri.common.manage.JMXUtils;
import com.bagri.xdm.XDMNode;
import com.bagri.xdm.access.api.XDMNodeManager;
import com.bagri.xdm.process.hazelcast.NodeOptionSetter;
import com.bagri.xdm.process.hazelcast.SchemaDenitiator;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.core.IMap;
import com.hazelcast.core.Member;

public class NodeManager implements InitializingBean, NodeManagerMBean, XDMNodeManager {

    private static final transient Logger logger = LoggerFactory.getLogger(NodeManager.class);
	private static final String type_node = "Node";

	private String nodeName;
    private HazelcastInstance hzInstance;
	private IExecutorService execService;
    private IMap<String, XDMNode> nodeCache;
    
	public NodeManager(HazelcastInstance hzInstance, String nodeName) {
		this.hzInstance = hzInstance;
		this.nodeName = nodeName;
		execService = hzInstance.getExecutorService("xdm-exec-pool");
		nodeCache = hzInstance.getMap("nodes");
	}
	
	@Override
	public void afterPropertiesSet() throws Exception {
		JMXUtils.registerMBean(type_node, nodeName, this);
	}
	
	public void close() {
		JMXUtils.unregisterMBean(type_node, nodeName);
	}

	@Override
	public String getNodeId() {
		return getNode().getId();
	}
	
	@Override
	public String getAddress() {
		return getNode().getAddress();
	}
	
	public Properties getOpts() {
		return getNode().getOptions();
	}

	@Override
	public CompositeData getOptions() {
		Properties options = getOpts();
		return JMXUtils.propsToComposite(nodeName, "options", options);
	}

	@Override
	public String getOption(String name) {
		return getOpts().getProperty(name);
	}

	@Override
	public void setOption(String name, String value) {
		XDMNode node = setNodeOption(name, value);
		Member member = getMember(node.getId());
		if (member.localMember()) {
			member.setStringAttribute(name, value);
		} else {
			logger.trace("setOption; distribute option set to node: {}", member);
			NodeOptionSetter nos = new NodeOptionSetter(node.getNode(), name, value);
			Future<Boolean> result = execService.submitToMember(nos, member);
			try {
				Boolean ok = result.get();
				logger.debug("setOption; distributed option set: {}", ok);
			} catch (InterruptedException | ExecutionException ex) {
				logger.error("setOption.error; ", ex);
			}
		}
	}

	public XDMNode setNodeOption(String name, String value) {
		XDMNode node = getNode();
		node.setOption(name, value);
		flushNode(node);
		return node;
	}

	@Override
	public void removeOption(String name) {
		// set to default value? or remove..
		XDMNode node = getNode();
		node.setOption(name, null);
		flushNode(node);

		Member member = getMember(node.getId());
		member.removeAttribute(name);
	}

	@Override
	public String[] getDeployedSchemas() {
		XDMNode node = getNode();
		return node.getSchemas();
	}
	
	private Member getMember(String uuid) {
		for (Member member: hzInstance.getCluster().getMembers()) {
			if (uuid.equals(member.getUuid())) {
				return member;
			}
		}
		return null;
	}


	//@Override
	protected XDMNode getNode() {
		XDMNode node = nodeCache.get(nodeName);
		logger.trace("getNode. returning: {}", node);
		return node;
	}

	//@Override
	protected void flushNode(XDMNode node) {
		nodeCache.put(nodeName, node);
	}
	
}
