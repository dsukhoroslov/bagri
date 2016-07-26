package com.bagri.xdm.cache.hazelcast.management;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

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

import com.bagri.common.util.JMXUtils;
import com.bagri.xdm.cache.hazelcast.BagriCacheServer;
import com.bagri.xdm.cache.hazelcast.task.node.NodeInfoProvider;
import com.bagri.xdm.cache.hazelcast.task.node.NodeKiller;
import com.bagri.xdm.cache.hazelcast.task.node.NodeOptionSetter;
import com.bagri.xdm.cache.hazelcast.task.node.NodeInfoProvider.InfoType;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.core.Member;

import static com.bagri.xdm.cache.hazelcast.util.HazelcastUtils.getMemberSchemas;
import static com.bagri.xdm.common.Constants.xdm_cluster_login;
import static com.bagri.xdm.common.Constants.xdm_cluster_node_name;
import static com.bagri.xdm.common.Constants.xdm_cluster_node_role;

@ManagedResource(description="Topology Manager MBean")
public class TopologyManager implements SelfNaming {

    protected static final Logger logger = LoggerFactory.getLogger(TopologyManager.class);
	
	private Member member;
	private IExecutorService execService;
	private HazelcastInstance hzInstance; 
	
	public TopologyManager(HazelcastInstance hzInstance, IExecutorService execService, Member member) {
		this.hzInstance = hzInstance;
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
		return member.getStringAttribute(xdm_cluster_node_name);
	}
	
	@ManagedAttribute(description="Returns active Node configuration role")
	public String getRole() {
		return member.getStringAttribute(xdm_cluster_node_role);
	}

	@ManagedAttribute(description="Returns active Node version")
	public String getVersion() {
		
		Class clazz = BagriCacheServer.class;
		String className = clazz.getSimpleName() + ".class";
		String classPath = clazz.getResource(className).toString();
		if (!classPath.startsWith("jar")) {
		  // Class not from JAR
			return null;
		}
		String manifestPath = classPath.substring(0, classPath.lastIndexOf("!") + 1) + "/META-INF/MANIFEST.MF";
		Manifest manifest;

		try (InputStream is = new URL(manifestPath).openStream()) {
			manifest = new Manifest(is);
			Attributes attr = manifest.getMainAttributes();
			String version = attr.getValue("Implementation-Version");
			String number = attr.getValue("Build-Number");
			if (number == null) {
				return version;
			}
			if (version == null) {
				return number;
			}
			return version + ":" + number;
		} catch (IOException ex) {
			logger.error("getVersion.error", ex); 
		}
		return null;
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
		String nodeName = member.getStringAttribute(xdm_cluster_node_name);
		logger.trace("setOption; nodeName: {}; options: {}", nodeName, props);
		String login = ((Member) hzInstance.getLocalEndpoint()).getStringAttribute(xdm_cluster_login);
		NodeOptionSetter setter = new NodeOptionSetter(JMXUtils.getCurrentUser(login), 
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
