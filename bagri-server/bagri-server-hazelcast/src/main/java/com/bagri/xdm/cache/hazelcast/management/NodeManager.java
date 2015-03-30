package com.bagri.xdm.cache.hazelcast.management;

import static com.bagri.xdm.client.common.XDMCacheConstants.PN_XDM_SYSTEM_POOL;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.management.openmbean.CompositeData;

import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;

import com.bagri.common.config.XDMConfigConstants;
import com.bagri.common.manage.JMXUtils;
import com.bagri.xdm.cache.hazelcast.task.node.NodeUpdater;
import com.bagri.xdm.system.XDMNode;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.core.Member;

@ManagedResource(description="Cluster Node Manager MBean")
public class NodeManager extends EntityManager<XDMNode> { //implements XDMNodeManager {

    private HazelcastInstance hzInstance;
	//private IExecutorService execService;

	public NodeManager() {
		// default constructor
		super();
	}
    
	public NodeManager(HazelcastInstance hzInstance, String nodeName) {
		super(nodeName);
		this.hzInstance = hzInstance;
		//execService = hzInstance.getExecutorService(PN_XDM_SYSTEM_POOL);
		//IMap<String, XDMNode> nodes = hzInstance.getMap("nodes"); 
		//setEntityCache(nodes);
	}
	
	@Override
	protected String getEntityType() {
		return "Node";
	}
	
	@ManagedAttribute(description="Returns registered Node name")
	public String getName() {
		// this is not an entityName!
		return getEntity().getName();
	}
	
	@ManagedAttribute(description="Returns active Node identifier")
	public String[] getNodeIds() {
		XDMNode node = getEntity();
		List<Member> members = getMembers(node.getName());
		if (members.size() > 0) {
			int i = 0;
			String[] ids = new String[members.size()];
			for (Member member: members) {
				ids[i++] = member.getUuid();
			}
			return ids;
		}
		return null;
	}
	
	@ManagedAttribute(description="Returns Node state")
	public boolean isActive() {
		XDMNode node = getEntity();
		List<Member> members = getMembers(node.getName());
		return members.size() > 0;
	}
	
	public Properties getOpts() {
		return getEntity().getOptions();
	}

	@ManagedAttribute(description="Returns registered Node options")
	public CompositeData getOptions() {
		Properties options = getOpts();
		return JMXUtils.propsToComposite(entityName, "options", options);
	}

	//@Override
	@ManagedOperation(description="Returns named Node option")
	@ManagedOperationParameters({
		@ManagedOperationParameter(name = "name", description = "A name of the option to return")})
	public String getOption(String name) {
		return getOpts().getProperty(name);
	}

	@ManagedOperation(description="Set named Node option")
	@ManagedOperationParameters({
		@ManagedOperationParameter(name = "name", description = "A name of the option to set"),
		@ManagedOperationParameter(name = "value", description = "A value of the option to set")})
	public void setOption(String name, String value) {
		XDMNode node = getEntity();
		if (node != null) {
			Properties opts = new Properties();
			opts.setProperty(name, value);
	    	Object result = entityCache.executeOnKey(entityName, new NodeUpdater(node.getVersion(), 
	    			JMXUtils.getCurrentUser(), "Option " + name + " set from JMX console", false, opts));
	    	logger.trace("setProperty; execution result: {}", result);
		}
	}

	//@Override
	@ManagedOperation(description="Removes named Node option")
	@ManagedOperationParameters({
		@ManagedOperationParameter(name = "name", description = "A name of the option to remove")})
	public void removeOption(String name) {
		XDMNode node = getEntity();
		if (node != null) {
			Properties opts = node.getOptions();
			opts.remove(name); // is it safe??
	    	Object result = entityCache.executeOnKey(entityName, new NodeUpdater(node.getVersion(), 
	    			JMXUtils.getCurrentUser(), "Option " + name + " removed from JMX console", true, opts));
	    	logger.trace("removeProperty; execution result: {}", result);
		}
	}

	@ManagedAttribute(description="Return Schema names deployed on the Node")
	public String[] getDeployedSchemas() {
		XDMNode node = getEntity();
		return node.getSchemas();
	}
	
	private List<Member> getMembers(String name) {
		List<Member> members = new ArrayList<Member>();
		for (Member member: hzInstance.getCluster().getMembers()) {
			if (name.equals(member.getStringAttribute(XDMConfigConstants.xdm_cluster_node_name))) {
				members.add(member);
			}
		}
		return members;
	}
	
	@ManagedOperation(description="Initiates schema on Cluster node(-s)")
	@ManagedOperationParameters({
		@ManagedOperationParameter(name = "schemaName", description = "Schema name to add")})
	public void addSchema(String schemaName) {
		XDMNode node = getEntity();
		String schemas = node.getOption(XDMConfigConstants.xdm_cluster_node_schemas);
		if (schemas != null) {
			if (schemas.length() > 0) {
				schemas = schemas + " " + schemaName;
			} else {
				schemas = schemaName;
			}
		} else {
			schemas = schemaName;
		}
		
		setOption(XDMConfigConstants.xdm_cluster_node_schemas, schemas);
	}

	@ManagedOperation(description="Returns Node configurations")
	public String generateOptions() {
		Properties options = getOpts();
		StringBuilder result = new StringBuilder();
		for (String name: options.stringPropertyNames()) {
			result.append(name).append("=").append(options.getProperty(name));
			result.append("\n");
		}
		return result.toString();
	}
	
}
