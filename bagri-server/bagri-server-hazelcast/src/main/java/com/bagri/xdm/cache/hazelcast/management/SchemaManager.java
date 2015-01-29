package com.bagri.xdm.cache.hazelcast.management;

import java.io.IOException;
import java.util.Collection;
import java.util.Properties;
import java.util.Set;

import javax.management.openmbean.CompositeData;

import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;

import com.bagri.common.manage.JMXUtils;
import com.bagri.common.util.FileUtils;
import com.bagri.common.util.PropUtils;
import com.bagri.xdm.api.XDMModelManagement;
import com.bagri.xdm.cache.common.XDMDocumentManagementServer;
import com.bagri.xdm.cache.hazelcast.task.schema.SchemaActivator;
import com.bagri.xdm.cache.hazelcast.task.schema.SchemaUpdater;
import com.bagri.xdm.client.common.impl.XDMModelManagementBase;
import com.bagri.xdm.system.XDMSchema;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.Member;
import com.hazelcast.instance.MemberImpl;

import static com.bagri.xdm.client.common.XDMConfigConstants.xdm_schema_store_type;

@ManagedResource(description="Schema Manager MBean")
//public class SchemaManager extends XDMSchemaManagerBase implements SelfNaming {
public class SchemaManager extends EntityManager<XDMSchema> {

    private static final String state_ok = "working";
    private static final String state_fail = "inactive";

    private SchemaManagement parent;
	protected XDMDocumentManagementServer docManager;
	protected XDMModelManagement schemaDictionary;
	private ClassPathXmlApplicationContext clientContext;
    
	public SchemaManager() {
		super();
	}

	public SchemaManager(SchemaManagement parent, String schemaName) {
		super(schemaName);
		this.parent = parent;
	}
	
	public void setClientContext(ClassPathXmlApplicationContext clientContext) {
		this.clientContext = clientContext;
	}
	
	public XDMDocumentManagementServer getDocumentManager() {
		return docManager;
	}
	
	public void setDocumentManager(XDMDocumentManagementServer docManager) {
		this.docManager = docManager;
	}
	
	public XDMModelManagement getSchemaDictionary() {
		return schemaDictionary;
	}
	
	public void setSchemaDictionary(XDMModelManagement schemaDictionary) {
		this.schemaDictionary = schemaDictionary;
	}
	
	@ManagedAttribute(description="Returns active schema nodes")
	public String[] getActiveNodes() {
		if (clientContext == null) {
			return new String[0];
		}
		HazelcastInstance hzInstance = clientContext.getBean("hzInstance", HazelcastInstance.class);
		logger.trace("getActiveNodes; client: {}", hzInstance);
		if (hzInstance instanceof HazelcastClient) {
			Collection<MemberImpl> members = ((HazelcastClient) hzInstance).getClientClusterService().getMemberList();
			String[] result = new String[members.size()];
			int idx = 0;
			for (Member member: members) {
				result[idx++] = member.getSocketAddress().toString();
			}
			return result;
		} else {
			Set<Member> members = hzInstance.getCluster().getMembers();
			String[] result = new String[members.size()];
			int idx = 0;
			for (Member member: members) {
				result[idx++] = member.getSocketAddress().toString();
			}
			return result;
		}
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
	
	@ManagedAttribute(description="Returns Schema persistence type")
	public String getPersistenceType() {
		String result = getEntity().getProperty(xdm_schema_store_type);
		if (result == null) {
			result = "NONE";
		}
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

	@ManagedAttribute(description="Returns Schema state")
	public boolean isActive() {
		return getEntity().isActive(); //?? prabably we have to calc it..
	}

	@ManagedAttribute(description="Returns registered Schema state")
	public String getState() {
		if (clientContext == null) {
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
			String user = JMXUtils.getCurrentUser();
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
			String user = JMXUtils.getCurrentUser();
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
	    			JMXUtils.getCurrentUser(), false, props));
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
	    			JMXUtils.getCurrentUser(), true, props));
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

}
