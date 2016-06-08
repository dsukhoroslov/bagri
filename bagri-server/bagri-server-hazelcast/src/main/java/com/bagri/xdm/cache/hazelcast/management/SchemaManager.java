package com.bagri.xdm.cache.hazelcast.management;

import static com.bagri.xdm.cache.api.XDMCacheConstants.PN_XDM_SCHEMA_POOL;
import static com.bagri.xdm.cache.api.XDMRepository.bean_id;
import static com.bagri.xdm.common.XDMConstants.xdm_schema_store_enabled;
import static com.bagri.xdm.common.XDMConstants.xdm_schema_store_type;
import static com.bagri.xdm.common.XDMConstants.xs_ns;
import static com.bagri.xdm.common.XDMConstants.xs_prefix;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

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

import com.bagri.common.manage.JMXUtils;
import com.bagri.common.util.PropUtils;
import com.bagri.xdm.api.XDMHealthChangeListener;
import com.bagri.xdm.api.XDMHealthState;
import com.bagri.xdm.api.XDMRepository;
import com.bagri.xdm.cache.hazelcast.task.schema.SchemaActivator;
import com.bagri.xdm.cache.hazelcast.task.schema.SchemaHealthAggregator;
import com.bagri.xdm.cache.hazelcast.task.schema.SchemaPopulator;
import com.bagri.xdm.cache.hazelcast.task.schema.SchemaUpdater;
import com.bagri.xdm.cache.hazelcast.util.HazelcastUtils;
import com.bagri.xdm.system.XDMCollection;
import com.bagri.xdm.system.XDMFragment;
import com.bagri.xdm.system.XDMIndex;
import com.bagri.xdm.system.XDMJavaTrigger;
import com.bagri.xdm.system.XDMSchema;
import com.bagri.xdm.system.XDMTriggerAction;
import com.bagri.xdm.system.XDMTriggerDef;
import com.bagri.xdm.system.XDMXQueryTrigger;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.impl.HazelcastClientInstanceImpl;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.core.Member;

@ManagedResource(description="Schema Manager MBean")
public class SchemaManager extends EntityManager<XDMSchema> implements XDMHealthChangeListener {

    private static final String state_ok = "working";
    private static final String state_fail = "inactive";

    private XDMRepository xdmRepo;
    private SchemaManagement parent;
    private XDMHealthState hState;
	private IExecutorService execService;
	private HazelcastInstance schemaInstance;
    
	public SchemaManager() {
		super();
	}

	public SchemaManager(HazelcastInstance hzInstance, String schemaName, SchemaManagement parent) {
		super(hzInstance, schemaName);
		this.parent = parent;
	}
	
	HazelcastInstance getHazelcastClient() {
		return schemaInstance;
	}

	XDMRepository getRepository() {
		return xdmRepo;
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
			setRepository((XDMRepository) schemaInstance.getUserContext().get(bean_id));
		}
	}
	
	void setRepository(XDMRepository xdmRepo) {
		this.xdmRepo = xdmRepo;
		this.hState = xdmRepo.getHealthManagement().getHealthState();
		xdmRepo.getHealthManagement().addHealthChangeListener(this);
	}
	
	@ManagedAttribute(description="Returns active schema nodes")
	public String[] getActiveNodes() {
		if (schemaInstance == null) {
			return new String[0];
		}
		Collection<Member> members;
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
		String value = getEntity().getProperty(xdm_schema_store_enabled);
		return "true".equalsIgnoreCase(value);
	}

	@ManagedAttribute(description="Returns Schema persistence format")
	public String getDataFormat() {
		return getEntity().getProperty(xdm_schema_store_type);
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
                result = JMXUtils.compositeToTabular("Health", "Desc", "Member", result, counters);
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
		return JMXUtils.propsToComposite(entityName, "properties", props);
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
	
	//void setState(String state) {
	//	this.state = state;
	//}
	
	@ManagedOperation(description="Activate Schema")
	public boolean activateSchema() {
		XDMSchema schema = getEntity();
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
		XDMSchema schema = getEntity();
		if (schema != null && schema.isActive()) {
			String user = getCurrentUser();
	    	Object result = entityCache.executeOnKey(entityName, new SchemaActivator(schema.getVersion(), user, false));
	    	logger.trace("deactivateSchema; execution result: {}", result);
	    	return result != null;
		}
		return false;
	}

	//@Override
	@ManagedOperation(description="Returns named Schema property")
	@ManagedOperationParameters({
		@ManagedOperationParameter(name = "name", description = "A name of the property to return")})
	public String getProperty(String name) {
		return getEntity().getProperty(name);
	}
	
	//@Override
	@ManagedOperation(description="Set named Schema property")
	@ManagedOperationParameters({
		@ManagedOperationParameter(name = "name", description = "A name of the property to set"),
		@ManagedOperationParameter(name = "value", description = "A value of the property to set")})
	public void setProperty(String name, String value) {
		XDMSchema schema = getEntity();
		if (schema != null) {
			Properties props = new Properties();
			props.setProperty(name, value);
	    	Object result = entityCache.executeOnKey(entityName, new SchemaUpdater(schema.getVersion(), 
	    			getCurrentUser(), false, props));
	    	logger.trace("setProperty; execution result: {}", result);
		}
	}
	
	//@Override
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
		@ManagedOperationParameter(name = "properties", description = "Schema properties: key/value pairs separated by comma")})
	public boolean updateProperties(String properties) {
		XDMSchema schema = getEntity();
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
	
	@ManagedOperation(description="Initiates schema population process")
	public void populateSchema() {
		if (!isPersistent()) {
			// throw ex?
			return;
		}
		SchemaPopulator pop = new SchemaPopulator(entityName);
		execService.submitToAllMembers(pop);
	}

	XDMCollection addCollection(String name, String docType, String description) {
		XDMSchema schema = getEntity();
		int id = 0; 
		for (XDMCollection collect: schema.getCollections()) {
			if (collect.getId() > id) {
				id = collect.getId(); 
			}
		}
		id++;
		XDMCollection collection = new XDMCollection(1, new Date(), getCurrentUser(), id, name, docType, description, true);
		if (schema.addCollection(collection)) {
			// store schema!
			flushEntity(schema);
			return collection;
		}
		return null;
	}
	
	boolean deleteCollection(String name) {
		XDMSchema schema = getEntity();
		if (schema.removeCollection(name) != null) {
			// store schema!
			flushEntity(schema);
			return true;
		}
		return false;
	}

	boolean enableCollection(String name, boolean enable) {
		XDMSchema schema = getEntity();
		if (schema.enableCollection(name, enable)) {
			// store schema!
			flushEntity(schema);
			return true;
		}
		return false;
	}

	XDMFragment addFragment(String name, String docType, String path, String description) {
		//String typePath = schemaDictionary.normalizePath(docType);
		XDMFragment fragment = new XDMFragment(1, new Date(), getCurrentUser(), name, docType, //typePath, 
				path, description, true);
		XDMSchema schema = getEntity();
		if (schema.addFragment(fragment)) {
			// store schema!
			flushEntity(schema);
			return fragment;
		}
		return null;
	}
	
	boolean deleteFragment(String name) {
		XDMSchema schema = getEntity();
		if (schema.removeFragment(name) != null) {
			// store schema!
			flushEntity(schema);
			return true;
		}
		return false;
	}

	boolean enableFragment(String name, boolean enable) {
		XDMSchema schema = getEntity();
		if (schema.enableFragment(name, enable)) {
			// store schema!
			flushEntity(schema);
			return true;
		}
		return false;
	}

	XDMIndex addIndex(String name, String docType, String path, String dataType, boolean caseSensitive, boolean range, 
			boolean unique, String description) {
		String typePath = xdmRepo.getModelManagement().normalizePath(docType);
		XDMIndex index = new XDMIndex(1, new Date(), getCurrentUser(), name, docType, typePath, 
				path, new QName(xs_ns, dataType, xs_prefix), caseSensitive, range, unique, description, true);
		XDMSchema schema = getEntity();
		if (schema.addIndex(index)) {
			// store schema!
			flushEntity(schema);
			return index;
		}
		return null;
	}
	
	boolean deleteIndex(String name) {
		XDMSchema schema = getEntity();
		if (schema.removeIndex(name) != null) {
			// store schema!
			flushEntity(schema);
			return true;
		}
		return false;
	}

	boolean enableIndex(String name, boolean enable) {
		XDMSchema schema = getEntity();
		if (schema.enableIndex(name, enable)) {
			// store schema!
			flushEntity(schema);
			return true;
		}
		return false;
	}

	XDMTriggerDef addTrigger(boolean java, String container, String implementation, String docType, 
			boolean synchronous, Collection<XDMTriggerAction> actions, int index) {
		XDMTriggerDef trigger;
		if (java) {
			trigger = new XDMJavaTrigger(1, new Date(), getCurrentUser(), container, 
				 implementation, docType, synchronous, true, index);
		} else {
			trigger = new XDMXQueryTrigger(1, new Date(), getCurrentUser(), container, 
					 implementation, docType, synchronous, true, index);
		}
		trigger.setActions(actions);
		XDMSchema schema = getEntity();
		if (schema.addTrigger(trigger)) {
			// store schema!
			flushEntity(schema);
			return trigger;
		}
		return null;
	}
	
	boolean deleteTrigger(String name) {
		XDMSchema schema = getEntity();
		if (schema.removeTrigger(name) != null) {
			// store schema!
			flushEntity(schema);
			return true;
		}
		return false;
	}

	boolean enableTrigger(String name, boolean enable) {
		XDMSchema schema = getEntity();
		if (schema.enableTrigger(name, enable)) {
			// store schema!
			flushEntity(schema);
			return true;
		}
		return false;
	}

	@Override
	public void onHealthStateChange(XDMHealthState newState) {
		this.hState = newState;
	}

}
