package com.bagri.server.hazelcast.management;

import static com.bagri.core.Constants.*;
import static com.bagri.core.server.api.CacheConstants.*;
import static com.bagri.core.server.api.SchemaRepository.bean_id;
import static com.bagri.support.util.JMXUtils.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.Date;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.management.Notification;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.TabularData;
import javax.xml.namespace.QName;

import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.jmx.export.notification.NotificationPublisher;
import org.springframework.jmx.export.notification.NotificationPublisherAware;

import com.bagri.client.hazelcast.PartitionStatistics;
import com.bagri.client.hazelcast.impl.SchemaRepositoryImpl;
import com.bagri.core.api.HealthChangeListener;
import com.bagri.core.api.HealthState;
import com.bagri.core.api.SchemaRepository;
import com.bagri.core.system.Collection;
import com.bagri.core.system.Fragment;
import com.bagri.core.system.Index;
import com.bagri.core.system.JavaTrigger;
import com.bagri.core.system.Node;
import com.bagri.core.system.Resource;
import com.bagri.core.system.Schema;
import com.bagri.core.system.TriggerAction;
import com.bagri.core.system.TriggerDefinition;
import com.bagri.core.system.XQueryTrigger;
import com.bagri.server.hazelcast.task.doc.DocumentQueueCounter;
import com.bagri.server.hazelcast.task.node.NodeDistributionProvider;
import com.bagri.server.hazelcast.task.node.NodeKiller;
import com.bagri.server.hazelcast.task.schema.SchemaActivator;
import com.bagri.server.hazelcast.task.schema.SchemaHealthAggregator;
import com.bagri.server.hazelcast.task.schema.SchemaUpdater;
import com.bagri.server.hazelcast.util.HazelcastUtils;
import com.bagri.support.util.PropUtils;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.impl.clientside.HazelcastClientInstanceImpl;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.core.IMap;
import com.hazelcast.core.Member;

@ManagedResource(description="Schema Manager MBean")
public class SchemaManager extends EntityManager<Schema> implements HealthChangeListener, NotificationPublisherAware {

    private static final String state_ok = "working";
    private static final String state_fail = "inactive";

    private SchemaRepository xdmRepo;
    private SchemaManagement parent;
    private HealthState hState;
	private IExecutorService execService;
	private HazelcastInstance schemaInstance;

	private int jmxIndex = 0;
	private NotificationPublisher jmxPublisher;
    
	public SchemaManager() {
		super();
	}

	public SchemaManager(HazelcastInstance hzInstance, String schemaName, SchemaManagement parent) {
		super(hzInstance, schemaName);
		this.parent = parent;
	}
	
	@Override
	public void setNotificationPublisher(NotificationPublisher notificationPublisher) {
		jmxPublisher = notificationPublisher; 
	}
	
	HazelcastInstance getHazelcastClient() {
		return schemaInstance;
	}

	SchemaRepository getRepository() {
		return xdmRepo;
	}
	
	SchemaManagement getParent() {
		return parent;
	}
	
	public void setClientContext(ClassPathXmlApplicationContext clientContext) {
		if (clientContext == null) {
			if (xdmRepo != null) {
				xdmRepo.getHealthManagement().removeHealthChangeListener(this);
				xdmRepo.close();
				xdmRepo = null;
			}
			execService = null;
			schemaInstance = null; // shutdown ?
		} else {
			//schemaInstance = clientContext.getBean(hz_instance, HazelcastInstance.class);
			schemaInstance = HazelcastUtils.getHazelcastClientByName(entityName);
			logger.trace("setClientContext; got HZ instance: {}, from {}", schemaInstance, HazelcastClient.getAllHazelcastClients());
			execService = schemaInstance.getExecutorService(PN_XDM_SCHEMA_POOL);
			//setRepository(clientContext.getBean(XDMRepository.class));
			setRepository((SchemaRepository) schemaInstance.getUserContext().get(bean_id));
		}
	}
	
	void setRepository(SchemaRepository xdmRepo) {
		this.xdmRepo = xdmRepo;
		this.hState = xdmRepo.getHealthManagement().getHealthState();
		xdmRepo.getHealthManagement().addHealthChangeListener(this);
		((SchemaRepositoryImpl) xdmRepo).setStaticRepository();
	}
	
	@ManagedAttribute(description="Returns active schema nodes")
	public String[] getActiveNodes() {
		if (schemaInstance == null) {
			return new String[0];
		}
		java.util.Collection<Member> members;
		if (schemaInstance instanceof HazelcastClientInstanceImpl) {
			members = ((HazelcastClientInstanceImpl) schemaInstance).getClientClusterService().getMemberList();
		} else {
			members = schemaInstance.getCluster().getMembers();
		}
		String[] result = new String[members.size()];
		int idx = 0;
		for (Member member: members) {
			result[idx++] = member.getSocketAddress().toString();
		}
		return result;
	}

	@ManagedAttribute(description="Returns short Schema description")
	public String getDescription() {
		return getEntity().getDescription();
	}

	//@ManagedAttribute(description="Returns short Schema description")
	//public void setDescription(String description) {
	//	XDMSchema schema = getEntity();
	//	schema.setDescription(description);
	//	flushEntity(schema);
	//}
	
	@ManagedAttribute(description="Returns true if Schema persist its documents")
	public boolean isPersistent() {
		String value = getEntity().getProperty(pn_schema_store_enabled);
		return "true".equalsIgnoreCase(value);
	}

	@ManagedAttribute(description="Returns Schema persistence format")
	public String getDataFormat() {
		return getEntity().getProperty(pn_schema_format_default);
	}

	@ManagedAttribute(description="Returns Schema health state")
	public String getHealthState() {
		if (hState != null) {
			return hState.toString();
		}
		return state_fail;
	}

	@ManagedAttribute(description="Returns HealthManagement statistics, per node")
	public TabularData getHealthStatistics() {
		logger.trace("getHealthStatistics.enter;");
		int cnt = 0;
		TabularData result = null;
		Callable<CompositeData> task = new SchemaHealthAggregator();
		Map<Member, Future<CompositeData>> futures = execService.submitToAllMembers(task);
		
		for (Map.Entry<Member, Future<CompositeData>> entry: futures.entrySet()) {
			try {
				CompositeData counters = entry.getValue().get();
				logger.trace("getHealthStatistics; got counters: {}, from member {}", counters, entry.getKey());
                result = compositeToTabular("Health", "Desc", "Member", result, counters);
				logger.trace("getHealthStatistics; got aggregated result: {}", result);
				cnt++;
			} catch (InterruptedException | ExecutionException | OpenDataException ex) {
				logger.error("getHealthStatistics.error: " + ex.getMessage(), ex);
			}
		}
		logger.trace("getHealthStatistics.exit; got stats from {} nodes", cnt);
		return result;
	}
	
	@ManagedAttribute(description="Returns registered Schema name")
	public String getName() {
		return entityName;
	}
	
	@ManagedAttribute(description="Returns registered Schema properties")
	public CompositeData getProperties() {
		Properties props = getEntity().getProperties();
		return propsToComposite(entityName, "properties", props);
	}
	
	@ManagedAttribute(description="Returns registered Schema version")
	public int getVersion() {
		return getEntity().getVersion();
	}

	@ManagedAttribute(description="Returns registered Schema activity")
	public boolean isActive() {
		return getEntity().isActive(); //?? probably we have to calc it..
	}

	@ManagedAttribute(description="Returns registered Schema state")
	public String getState() {
		if (schemaInstance == null) {
			return state_fail;
		}
		return state_ok;
	}

	@ManagedOperation(description="Start Schema cluster")
	@ManagedOperationParameters({
		@ManagedOperationParameter(name = "node", description = "Schema node template"),
		@ManagedOperationParameter(name = "addresses", description = "Nodes to start schema on, separated by comma"),
		@ManagedOperationParameter(name = "nodesPerAddress", description = "Number of nodes started on each address"),
		@ManagedOperationParameter(name = "nodeSize", description = "Amount of memory allocated per node, in Gb")})
	public int startCluster(String nodeName, String addresses, int nodesPerAddress, int nodeSize) {
		Schema schema = getEntity();
		//if (schema != null && schema.isActive()) {
		// get node template for cache server (first)
		// get properties from the node
		// run command to go to server node: ssh user@address:password
		// cd to node bin path: cd /home/sdv/bagri/bin
		// run start script: ./bgcache.sh start fourth 1
		//
		// for Windows, assume we mount external boX? no..
		// run cmd command
		// cd to node bin path
		// run start script: bgcache.cmd fourth 0
		//
		// path memory size as command line param: bgcache.cmd fourth 3 16g
		//}
		
		IMap<String, Node> nodes = this.hzInstance.getMap(CN_SYS_NODES);
		Node node = nodes.get(nodeName);
		if (node == null) {
			return 0;
		}
		
		int started = 0;
		String[] boxes = addresses.split(", ");
		if (boxes.length > 0 && nodesPerAddress > 0 && nodeSize > 0) {
			//String bStart = "cmd /K start \"bagri server\" /D %path% bgcache.cmd empty %num% %size%";
			String bStart = node.getOption(pn_cluster_node_start);
			String bHome = node.getOption(pn_cluster_node_home);
			String bSsh = node.getOption(pn_cluster_node_ssh);
			String bUser = null;
			boolean useSsh = false;
			if (bSsh != null) {
				useSsh = Boolean.parseBoolean(bSsh);
				if (useSsh) {
					bUser = node.getOption(pn_cluster_node_user);
					useSsh = bUser != null; 
				}
			}
			String size = nodeSize + "g";
			for (String box: boxes) {
				for (int cnt = 1; cnt <= nodesPerAddress; cnt++) {
					try {
						//String os = System.getProperty("os.name"); // Linux/Windows
						if (useSsh) {
							String cmd = "/home/dsukhoroslov/git/bagri/start_node.sh"; // "ssh " + bUser + "@" + box;
							Process pro = Runtime.getRuntime().exec("lxterminal -e " + cmd);
							PrintStream out = new PrintStream(pro.getOutputStream(), true);
							BufferedReader in = new BufferedReader(new InputStreamReader(pro.getInputStream()));
							//out.println(cmd);
							logger.info("startCluster; ssh: {}", cmd);
							cmd = "cd " + bHome + "/bin";
							//out.println(cmd);
							logger.info("startCluster; dir: {}", cmd);
							cmd = bStart + " start empty " + String.valueOf(started) + " " + size;
							//out.println(cmd);
							logger.info("startCluster; cmd: {}", cmd);
							while (in.ready()) {
								String s = in.readLine();
								logger.info("startCluster; got input: {}", s);
							}
							out.println("exit");						
						} else {
							ProcessBuilder pb = new ProcessBuilder(bStart, "start", "empty", String.valueOf(started), size);
							Map<String, String> env = pb.environment();
							env.put("bdb.cluster.members", box);
							env.put("bdb.cluster.node.schemas", entityName);
							pb.directory(new File(bHome + "/bin"));
							Process pro = pb.start();	
							//if (pro.isAlive()) {
							//	String cmd = bStart.replace("%path%", bHome); 
							//	cmd = cmd.replace("%num%", String.valueOf(started));
							//	cmd = cmd.replaceAll("%size%", size);
							//  cmd += " -Dbdb.cluster.members=" + box; // it must be admin node IP?
					    	//  cmd += " -Dbdb.cluster.node.schemas=" + entityName;
							//	logger.info("startCluster; cmd: {}", cmd);
							//	Process pro = Runtime.getRuntime().exec(cmd);
						}
						started++;
					} catch (IOException ex) {
						logger.error("startCluster.error;", ex);
					}
				}
			}
		}
		return started;
	}
	
	@ManagedOperation(description="Stop Schema cluster")
	public boolean stopCluster() {
		if (schemaInstance == null) {
			return false;
		}
		java.util.Collection<Member> members;
		if (schemaInstance instanceof HazelcastClientInstanceImpl) {
			members = ((HazelcastClientInstanceImpl) schemaInstance).getClientClusterService().getMemberList();
		} else {
			members = schemaInstance.getCluster().getMembers();
		}

        Runnable task = new NodeKiller(entityName);
        execService.executeOnMembers(task, members);
		return true;
	}
	
	@ManagedOperation(description="Activate Schema")
	public boolean activateSchema() {
		Schema schema = getEntity();
		if (schema != null && !schema.isActive()) {
			String user = getCurrentUser();
	    	Object result = entityCache.executeOnKey(entityName, new SchemaActivator(schema.getVersion(), user, true));
	    	logger.trace("activateSchema; execution result: {}", result);
	    	return result != null;
		} 
		return false;
	}
		
	@ManagedOperation(description="Deactivate Schema")
	public boolean deactivateSchema() {
		Schema schema = getEntity();
		if (schema != null && schema.isActive()) {
			String user = getCurrentUser();
	    	Object result = entityCache.executeOnKey(entityName, new SchemaActivator(schema.getVersion(), user, false));
	    	logger.trace("deactivateSchema; execution result: {}", result);
	    	return result != null;
		}
		return false;
	}

	@ManagedOperation(description="Returns named Schema property")
	@ManagedOperationParameters({
		@ManagedOperationParameter(name = "name", description = "A name of the property to return")})
	public String getProperty(String name) {
		return getEntity().getProperty(name);
	}
	
	@ManagedOperation(description="Set named Schema property")
	@ManagedOperationParameters({
		@ManagedOperationParameter(name = "name", description = "A name of the property to set"),
		@ManagedOperationParameter(name = "value", description = "A value of the property to set")})
	public void setProperty(String name, String value) {
		Schema schema = getEntity();
		if (schema != null) {
			Properties props = new Properties();
			props.setProperty(name, value);
	    	Object result = entityCache.executeOnKey(entityName, new SchemaUpdater(schema.getVersion(), 
	    			getCurrentUser(), false, props));
	    	logger.trace("setProperty; execution result: {}", result);
	    	if (result != null) { //schema updated
				String message = "Schema property " + name + " set to " + value;
				String jmxType = "bdb.jmx.notification.property";
				Notification note = new Notification(jmxType, this, jmxIndex++, System.currentTimeMillis(), message);
				note.setUserData(entityName);
				jmxPublisher.sendNotification(note);
	    	}
		}
	}
	
	@ManagedOperation(description="Removes named Schema property")
	@ManagedOperationParameters({
		@ManagedOperationParameter(name = "name", description = "A name of the property to remove")})
	public void removeProperty(String name) {
		// override property with default value
		String defValue = parent.getDefaultProperty(name);
		if (defValue == null) {
			defValue = ""; // throw exception ???
		}
		setProperty(name, defValue);
	}
	
	@ManagedOperation(description="Update Schema properties")
	@ManagedOperationParameters({
		@ManagedOperationParameter(name = "properties", description = "Schema properties: key/value pairs separated by semicolon")})
	public boolean updateProperties(String properties) {
		Schema schema = getEntity();
		if (schema != null) {
			Properties props;
			try {
				props = PropUtils.propsFromString(properties);
			} catch (IOException ex) {
				logger.error("updateProperties.error: ", ex);
				return false;
			}
			
	    	Object result = entityCache.executeOnKey(entityName, new SchemaUpdater(schema.getVersion(), 
	    			getCurrentUser(), true, props));
	    	logger.trace("updateProperties; execution result: {}", result);
	    	return result != null;
		}
		return false;
	}

	@Override
	protected String getEntityType() {
		return "Schema";
	}
	
	@ManagedOperation(description="Initiates schema on Cluster node(-s)")
	@ManagedOperationParameters({
		@ManagedOperationParameter(name = "nodeName", description = "Cluster node name")})
	public void initSchemaOnNode(String nodeName) {

		// do this via NodeManager!
		NodeManager nMgr = (NodeManager) parent.getClusterService().getNodeManager(nodeName);
		if (nMgr != null) {
			nMgr.addSchema(entityName);
		}
		// throw ex for wrong node name?
	}
	
	@ManagedOperation(description="Return number of not-stored-yet Documents")
	public int checkUpdatingDocuments() {
		DocumentQueueCounter task = new DocumentQueueCounter();
		Map<Member, Future<Integer>> results = execService.submitToAllMembers(task);
		int result = 0;
		for (Map.Entry<Member, Future<Integer>> entry: results.entrySet()) {
			try {
				result += entry.getValue().get();
			} catch (InterruptedException | ExecutionException ex) {
				logger.error("checkUpdatingDocuments.error; ", ex);
				//throw new RuntimeException(ex.getMessage());
			}
		}
		return result;
	}

	@ManagedOperation(description="Return partition-related statistics")
	@ManagedOperationParameters({
		@ManagedOperationParameter(name = "typeSwitch", description = "An int flag regulating stats granulatity. 0 = per partition, 1 = per node, 2 = per machine")})
	public TabularData getPartitionStatistics(int typeSwitch) {
		logger.debug("getPartitionStatistics.enter; switch is: {}", typeSwitch);
		NodeDistributionProvider task = new NodeDistributionProvider();
		Map<Member, Future<java.util.Collection<PartitionStatistics>>> results = execService.submitToAllMembers(task);
		TabularData result = null;
		java.util.Collection<PartitionStatistics> stats;
		for (Map.Entry<Member, Future<java.util.Collection<PartitionStatistics>>> entry: results.entrySet()) {
			try {
				stats = entry.getValue().get();
			} catch (InterruptedException | ExecutionException ex) {
				logger.error("getPartitionStatistics.error; ", ex);
				//throw new RuntimeException(ex.getMessage());
				continue;
			}
			
			try {
				for (PartitionStatistics sts: stats) {
					CompositeData cd = mapToComposite("stats", "parts", sts.toMap());
					result = compositeToTabular("stats", "parts", "partition", result, cd);
				}
			} catch (OpenDataException ex) {
				logger.error("getPartitionStatistics.error; ", ex);
				throw new RuntimeException(ex.getMessage());
			}
		}
		logger.debug("getPartitionStatistics.exit; result size: {}", result.size());
		return result;
	}
	
	@Override
	public void onHealthStateChange(HealthState newState) {
		String message = "Health state changed from " + hState + " to " + newState;
		this.hState = newState;

		String jmxType = "bdb.jmx.notification.health";
		Notification note = new Notification(jmxType, this, jmxIndex++, System.currentTimeMillis(), message);
		note.setUserData(entityName);
		jmxPublisher.sendNotification(note);
	}

	
	Collection addCollection(String name, String docType, String description) {
		Schema schema = getEntity();
		int id = 0; 
		for (Collection collect: schema.getCollections()) {
			if (collect.getId() > id) {
				id = collect.getId(); 
			}
		}
		id++;
		Collection collection = new Collection(1, new Date(), getCurrentUser(), id, name, docType, description, true);
		if (schema.addCollection(collection)) {
			// store schema!
			flushEntity(schema);
			return collection;
		}
		return null;
	}
	
	boolean deleteCollection(String name) {
		Schema schema = getEntity();
		if (schema.removeCollection(name) != null) {
			// store schema!
			flushEntity(schema);
			return true;
		}
		return false;
	}

	boolean enableCollection(String name, boolean enable) {
		Schema schema = getEntity();
		if (schema.enableCollection(name, enable)) {
			// store schema!
			flushEntity(schema);
			return true;
		}
		return false;
	}

	Fragment addFragment(String name, String docType, String path, String description) {
		//String typePath = schemaDictionary.normalizePath(docType);
		Fragment fragment = new Fragment(1, new Date(), getCurrentUser(), name, docType, //typePath, 
				path, description, true);
		Schema schema = getEntity();
		if (schema.addFragment(fragment)) {
			// store schema!
			flushEntity(schema);
			return fragment;
		}
		return null;
	}
	
	boolean deleteFragment(String name) {
		Schema schema = getEntity();
		if (schema.removeFragment(name) != null) {
			// store schema!
			flushEntity(schema);
			return true;
		}
		return false;
	}

	boolean enableFragment(String name, boolean enable) {
		Schema schema = getEntity();
		if (schema.enableFragment(name, enable)) {
			// store schema!
			flushEntity(schema);
			return true;
		}
		return false;
	}

	Index addIndex(String name, String docType, String path, String dataType, boolean caseSensitive, boolean range, 
			boolean unique, String description) {
		// TODO: what it is for?!
		String typePath = path; //xdmRepo.getModelManagement().normalizePath(docType);
		Index index = new Index(1, new Date(), getCurrentUser(), name, docType, typePath, 
				path, new QName(xs_ns, dataType, xs_prefix), caseSensitive, range, unique, description, true);
		Schema schema = getEntity();
		if (schema.addIndex(index)) {
			// store schema!
			flushEntity(schema);
			return index;
		}
		return null;
	}
	
	boolean deleteIndex(String name) {
		Schema schema = getEntity();
		if (schema.removeIndex(name) != null) {
			// store schema!
			flushEntity(schema);
			return true;
		}
		return false;
	}

	boolean enableIndex(String name, boolean enable) {
		Schema schema = getEntity();
		if (schema.enableIndex(name, enable)) {
			// store schema!
			flushEntity(schema);
			return true;
		}
		return false;
	}

	Resource addResource(String name, String path, String module, String description) {
		Resource resource = new Resource(1, new Date(), getCurrentUser(), name, path, description, module, true);
		Schema schema = getEntity();
		if (schema.addResource(resource)) {
			// store schema!
			flushEntity(schema);
			return resource;
		}
		return null;
	}
	
	boolean deleteResource(String name) {
		Schema schema = getEntity();
		if (schema.removeResource(name) != null) {
			// store schema!
			flushEntity(schema);
			return true;
		}
		return false;
	}

	boolean enableResource(String name, boolean enable) {
		Schema schema = getEntity();
		if (schema.enableResource(name, enable)) {
			// store schema!
			flushEntity(schema);
			return true;
		}
		return false;
	}
	
	TriggerDefinition addTrigger(boolean java, String container, String implementation,  
			boolean synchronous, String collection, java.util.Collection<TriggerAction> actions) {
		TriggerDefinition trigger;
		if (java) {
			trigger = new JavaTrigger(1, new Date(), getCurrentUser(), container, implementation, synchronous, true, collection);
		} else {
			trigger = new XQueryTrigger(1, new Date(), getCurrentUser(), container, implementation, synchronous, true, collection);
		}
		trigger.setActions(actions);
		Schema schema = getEntity();
		if (schema.addTrigger(trigger)) {
			// store schema!
			flushEntity(schema);
			return trigger;
		}
		return null;
	}
	
	boolean deleteTrigger(String name) {
		Schema schema = getEntity();
		if (schema.removeTrigger(name) != null) {
			// store schema!
			flushEntity(schema);
			return true;
		}
		return false;
	}

	boolean enableTrigger(String name, boolean enable) {
		Schema schema = getEntity();
		if (schema.enableTrigger(name, enable)) {
			// store schema!
			flushEntity(schema);
			return true;
		}
		return false;
	}

}

