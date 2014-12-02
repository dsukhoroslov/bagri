package com.bagri.xdm.cache.hazelcast.management;

import java.util.Map;
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
import com.bagri.xdm.process.hazelcast.node.NodeInfoProvider;
import com.bagri.xdm.process.hazelcast.node.NodeInfoProvider.InfoType;
import com.bagri.xdm.process.hazelcast.node.NodeKiller;
import com.bagri.xdm.process.hazelcast.node.NodeOptionSetter;
import com.bagri.xdm.system.XDMNode;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.core.Member;

import static com.bagri.xdm.process.hazelcast.util.HazelcastUtils.getMemberSchemas;

@ManagedResource(description="Topology Manager MBean")
public class TopologyManager implements SelfNaming {

    protected static final Logger logger = LoggerFactory.getLogger(TopologyManager.class);
	
	private Member member;
	private IExecutorService execService;
	//private HazelcastInstance hzInstance; 
	
	public TopologyManager(IExecutorService execService, Member member) {
		this.execService = execService;
		this.member = member;
	}
	
	@ManagedAttribute(description="Returns active Node id")
	public String getId() {
		return member.getUuid();
	}
	
	@ManagedAttribute(description="Returns active Node address")
	public String getAddress() {
		return member.getSocketAddress().toString();
	}
	
	@ManagedAttribute(description="Returns schemas deployed on the Node")
	public String[] getDeployedSchemas() {
		return getMemberSchemas(member);
	}	
	
	@ManagedAttribute(description="Returns active Node options")
	public CompositeData getOptions() {
		Map<String, Object> opts = member.getAttributes();
		return JMXUtils.mapToComposite(member.getUuid(), "options", opts);
	}
	
	@ManagedAttribute(description="Returns active Node configuration name")
	public String getName() {
		return member.getStringAttribute(XDMNode.op_node_name);
	}
	
	@ManagedAttribute(description="Returns active Node configuration role")
	public String getRole() {
		return member.getStringAttribute(XDMNode.op_node_role);
	}

	@ManagedAttribute(description="Returns active Node version")
	public String getVersion() {
		// TODO: implement it.. build number etc..
		return "0.1"; //member.getStringAttribute(XDMNode.op_node_version);
	}

	@ManagedAttribute(description="Returns active Node clients")
	public CompositeData getClientsInfo() {
		return getCompositeInfo(InfoType.client);
	}
	
	@ManagedAttribute(description="Returns active Member information")
	public String getMemberInfo() {
		return member.toString();
	}

	@ManagedAttribute(description="Returns active Node memory information")
	public CompositeData getMemoryInfo() {
		return getCompositeInfo(InfoType.memory);
	}
	
	@ManagedAttribute(description="Returns active Node timing information")
	public CompositeData getTimingInfo() {
		return getCompositeInfo(InfoType.timing);
	}

	private CompositeData getCompositeInfo(InfoType type) {
		NodeInfoProvider nip = new NodeInfoProvider(type);
		Future<CompositeData> result = execService.submitToMember(nip, member);
		try {
			CompositeData timing = result.get();
			return timing;
		} catch (InterruptedException | ExecutionException ex) {
			logger.error("getCompositeInfo.error; ", ex);
		}
		return null;
	}
	
	public static ObjectName getMemberName(Member member) throws MalformedObjectNameException {
		return JMXUtils.getObjectName("Topology", member.getUuid());
	}

	@Override
	public ObjectName getObjectName() throws MalformedObjectNameException {
		return getMemberName(member);
	}

	@ManagedOperation(description="Set active Node option")
	@ManagedOperationParameters({
		@ManagedOperationParameter(name = "name", description = "A name of the option to set"),
		@ManagedOperationParameter(name = "value", description = "A value of the option to set")})
	public void setOption(String name, String value) {
		logger.trace("setOption.enter; name: {}; value: {}", name, value);
		Map<String, Object> opts = member.getAttributes();
		Properties props = new Properties();
		for (Map.Entry<String, Object> entry: opts.entrySet()) {
			props.setProperty(entry.getKey(), entry.getValue().toString());
		}
		props.put(name, value);
		String nodeName = member.getStringAttribute(XDMNode.op_node_name);
		logger.trace("setOption; nodeName: {}; options: {}", nodeName, props);
		NodeOptionSetter setter = new NodeOptionSetter(JMXUtils.getCurrentUser(), 
				"Option " + name + " set from JMX console", props);
		Future<Boolean> result = execService.submitToMember(setter, member);
		try {
			Boolean ok = result.get();
			logger.trace("setOption.exit; result: {}", ok);
		} catch (InterruptedException | ExecutionException ex) {
			logger.error("setOption.error; ", ex);
		}
	}
	
	@ManagedOperation(description="Shuts down Node")
	public void shutdown() {
		NodeKiller task = new NodeKiller();
		execService.executeOnMember(task, member);
	}
	
}
