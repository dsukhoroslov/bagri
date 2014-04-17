package com.bagri.xdm.cache.hazelcast.management;

import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeData;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.jmx.export.naming.SelfNaming;

import com.bagri.common.manage.JMXUtils;
import com.bagri.xdm.XDMNode;
import com.bagri.xdm.access.api.XDMNodeManager;
import com.bagri.xdm.process.hazelcast.NodeOptionSetter;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.core.IMap;
import com.hazelcast.core.Member;

@ManagedResource(description="Cluster Node Manager MBean")
public class NodeManager implements SelfNaming, XDMNodeManager {

    private static final transient Logger logger = LoggerFactory.getLogger(NodeManager.class);
	private static final String type_node = "Node";

	private String nodeName;
    private HazelcastInstance hzInstance;
	private IExecutorService execService;
    private IMap<String, XDMNode> nodeCache;

	public NodeManager() {
		// default constructor
		super();
	}
    
	public NodeManager(HazelcastInstance hzInstance, String nodeName) {
		this.hzInstance = hzInstance;
		this.nodeName = nodeName;
		execService = hzInstance.getExecutorService("xdm-exec-pool");
		nodeCache = hzInstance.getMap("nodes");
	}
	
	@ManagedAttribute(description="Returns registered Node identifier")
	public String getNodeId() {
		return getNode().getId();
	}
	
	@ManagedAttribute(description="Returns registered Node location")
	public String getAddress() {
		return getNode().getAddress();
	}
	
	public Properties getOpts() {
		return getNode().getOptions();
	}

	@ManagedAttribute(description="Returns registered Node options")
	public CompositeData getOptions() {
		Properties options = getOpts();
		return JMXUtils.propsToComposite(nodeName, "options", options);
	}

	@Override
	@ManagedOperation(description="Returns named Node option")
	@ManagedOperationParameters({
		@ManagedOperationParameter(name = "name", description = "A name of the option to return")})
	public String getOption(String name) {
		return getOpts().getProperty(name);
	}

	@Override
	@ManagedOperation(description="Set named Node option")
	@ManagedOperationParameters({
		@ManagedOperationParameter(name = "name", description = "A name of the option to set"),
		@ManagedOperationParameter(name = "value", description = "A value of the option to set")})
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
	@ManagedOperation(description="Removes named Node option")
	@ManagedOperationParameters({
		@ManagedOperationParameter(name = "name", description = "A name of the option to remove")})
	public void removeOption(String name) {
		// set to default value? or remove..
		XDMNode node = getNode();
		node.setOption(name, null);
		flushNode(node);

		Member member = getMember(node.getId());
		member.removeAttribute(name);
	}

	@ManagedAttribute(description="Return Schema names deployed on the Node")
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

	@Override
	public ObjectName getObjectName() throws MalformedObjectNameException {
		logger.debug("getObjectName.enter; nodeName: {}", nodeName);
		return JMXUtils.getObjectName(type_node, nodeName);
	}
	
}
