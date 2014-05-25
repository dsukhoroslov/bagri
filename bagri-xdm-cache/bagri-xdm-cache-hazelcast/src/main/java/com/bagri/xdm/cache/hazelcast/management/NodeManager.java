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
import com.bagri.xdm.access.api.XDMNodeManager;
import com.bagri.xdm.process.hazelcast.NodeOptionSetter;
import com.bagri.xdm.system.XDMNode;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.core.IMap;
import com.hazelcast.core.Member;

@ManagedResource(description="Cluster Node Manager MBean")
public class NodeManager extends EntityManager<XDMNode> implements XDMNodeManager {

    private HazelcastInstance hzInstance;
	private IExecutorService execService;

	public NodeManager() {
		// default constructor
		super();
	}
    
	public NodeManager(HazelcastInstance hzInstance, String nodeName) {
		super(nodeName);
		this.hzInstance = hzInstance;
		execService = hzInstance.getExecutorService("xdm-exec-pool");
		//IMap<String, XDMNode> nodes = hzInstance.getMap("nodes"); 
		//setEntityCache(nodes);
	}
	
	@ManagedAttribute(description="Returns registered Node name")
	public String getName() {
		return entityName;
	}
	
	@ManagedAttribute(description="Returns registered Node location")
	public String getAddress() {
		return getEntity().getAddress();
	}
	
	@ManagedAttribute(description="Returns active Node identifier")
	public String getNodeId() {
		XDMNode node = getEntity();
		Member member = getMember(node.getName(), node.getAddress());
		return member.getUuid();
	}
	
	@ManagedAttribute(description="Returns Node state")
	public boolean isActive() {
		XDMNode node = getEntity();
		Member member = getMember(node.getName(), node.getAddress());
		return member != null;
	}
	
	public Properties getOpts() {
		return getEntity().getOptions();
	}

	@ManagedAttribute(description="Returns registered Node options")
	public CompositeData getOptions() {
		Properties options = getOpts();
		return JMXUtils.propsToComposite(entityName, "options", options);
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
		Member member = getMember(node.getName(), node.getAddress());
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
		
		// @TODO: do it via NodeUpdater!
	}

	public XDMNode setNodeOption(String name, String value) {
		XDMNode node = getEntity();
		node.setOption(name, value);
		flushEntity(node);
		return node;
	}

	@Override
	@ManagedOperation(description="Removes named Node option")
	@ManagedOperationParameters({
		@ManagedOperationParameter(name = "name", description = "A name of the option to remove")})
	public void removeOption(String name) {
		// set to default value? or remove..
		XDMNode node = getEntity();
		node.setOption(name, null);
		flushEntity(node);

		Member member = getMember(node.getName(), node.getAddress());
		member.removeAttribute(name);
	}

	@ManagedAttribute(description="Return Schema names deployed on the Node")
	public String[] getDeployedSchemas() {
		XDMNode node = getEntity();
		return node.getSchemas();
	}
	
	private Member getMember(String name, String address) {
		for (Member member: hzInstance.getCluster().getMembers()) {
			if (address.equals(member.getSocketAddress().getHostName()) &&
					name.equals(member.getStringAttribute(XDMNode.op_node_name))) {
				return member;
			}
		}
		return null;
	}

	@Override
	protected String getEntityType() {
		return "Node";
	}
	
}
