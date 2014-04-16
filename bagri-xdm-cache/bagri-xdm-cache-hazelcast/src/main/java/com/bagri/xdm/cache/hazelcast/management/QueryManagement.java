/**
 * 
 */
package com.bagri.xdm.cache.hazelcast.management;

import java.util.Hashtable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import com.bagri.common.manage.JMXUtils;
import com.bagri.xdm.access.api.XDMDocumentManagerServer;
import com.bagri.xdm.access.api.XDMSchemaDictionary;

/**
 * @author Denis Sukhoroslov
 *
 */
public class QueryManagement implements InitializingBean, DisposableBean, QueryManagementMBean {
	
    private static final transient Logger logger = LoggerFactory.getLogger(QueryManagement.class);
	//private static final String schema_management = "SchemaManagement";
    private static final String type_schema = "Schema";

	private XDMDocumentManagerServer docManager;
	private XDMSchemaDictionary schemaDictionary;
    
    private String schemaName;
    
    public QueryManagement(String schemaName) {
    	this.schemaName = schemaName;
    }

	@Override
	public void afterPropertiesSet() throws Exception {
		//domain:type=Server,name=server5
		//domain:type=Server.Application,Server=server5,name=app1

		Hashtable keys = JMXUtils.getStandardKeys(type_schema + ".Query", "QueryManagement");
		keys.put(type_schema, schemaName);
		JMXUtils.registerMBean(keys, this);
	}

	@Override
	public void destroy() throws Exception {
		Hashtable keys = JMXUtils.getStandardKeys(type_schema + ".Query", "QueryManagement");
		keys.put(type_schema, schemaName);
		JMXUtils.unregisterMBean(keys);
	}

	public void setDocumentManager(XDMDocumentManagerServer docManager) {
		this.docManager = docManager;
	}
	
	public void setSchemaDictionary(XDMSchemaDictionary schemaDictionary) {
		this.schemaDictionary = schemaDictionary;
	}
	
	
	/* (non-Javadoc)
	 * @see com.bagri.xdm.cache.hazelcast.management.QueryManagementMBean#getSchema()
	 */
	@Override
	public String getSchema() {
		return schemaName;
	}

	/* (non-Javadoc)
	 * @see com.bagri.xdm.cache.hazelcast.management.QueryManagementMBean#runQuery(java.lang.String)
	 */
	@Override
	public String runQuery(String query) {
		// TODO Auto-generated method stub
		return null;
	}

}
