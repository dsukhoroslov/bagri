package com.bagri.xdm.process.hazelcast.schema;

import static com.bagri.xdm.access.api.XDMConfigConstants.*;
import static com.bagri.xdm.access.hazelcast.pof.XDMDataSerializationFactory.cli_XDMInitSchemaTask;
import static com.bagri.xdm.access.hazelcast.pof.XDMDataSerializationFactory.factoryId;

import java.io.IOException;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.env.PropertiesPropertySource;

import com.bagri.xdm.access.api.XDMSchemaDictionary;
import com.bagri.xdm.process.hazelcast.DocumentManagementServer;
import com.bagri.xdm.process.hazelcast.SpringContextHolder;
import com.bagri.xdm.system.XDMIndex;
import com.bagri.xdm.system.XDMSchema;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;
import com.hazelcast.spring.context.SpringAware;

@SpringAware
public class SchemaInitiator implements Callable<Boolean>, IdentifiedDataSerializable { //extends SchemaDenitiator {

	protected final transient Logger logger = LoggerFactory.getLogger(SchemaInitiator.class);
	private XDMSchema schema;
	
	public SchemaInitiator() {
		//
	}

	public SchemaInitiator(XDMSchema schema) {
		this.schema = schema;
	}

	@Override
	public Boolean call() throws Exception {
		//return schemaManager.initSchema(schemaName, properties);
		
		String schemaName = schema.getName();
		HazelcastInstance hz = Hazelcast.getHazelcastInstanceByName(schemaName);
		if (hz != null) {
    		logger.debug("initSchema.exit; schema {} already started on instance: {}, returning", schemaName, hz);
    		return false;
		}
		
		Properties props = schema.getProperties();
    	props.setProperty(xdm_schema_name, schemaName);
    	PropertiesPropertySource pps = new PropertiesPropertySource(schemaName, props);
    	
    	try {
    		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext();
    		ctx.getEnvironment().getPropertySources().addFirst(pps);
            //String contextPath = System.getProperty(xdm_config_context_file);
    		//ctx.setConfigLocation(contextPath);
    		ctx.setConfigLocation("spring/cache-xqj-context.xml");
    		ctx.refresh();

    		hz = ctx.getBean("hzInstance", HazelcastInstance.class);
    		//hz.getConfig().getSecurityConfig().setEnabled(true);
			SpringContextHolder.setContext(schemaName, "appContext", ctx);
			
			Set<XDMIndex> indexes = schema.getIndexes();
			if (indexes.size() > 0) {
				DocumentManagementServer docMgr = ctx.getBean(DocumentManagementServer.class);
				XDMSchemaDictionary sDict = docMgr.getSchemaDictionary();
				for (XDMIndex idx: indexes) {
					sDict.createIndex(idx);
				}
			}
    		logger.debug("initSchema.exit; schema {} started on instance: {}", schemaName, hz);
    		return true;
    	} catch (Exception ex) {
    		logger.error("initSchema.error; " + ex.getMessage(), ex);
    		return false;
    	}
	}
	
	@Override
	public int getId() {
		return cli_XDMInitSchemaTask;
	}

	@Override
	public int getFactoryId() {
		return factoryId;
	}
	
	@Override
	public void readData(ObjectDataInput in) throws IOException {
		schema = in.readObject();
	}
	
	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		out.writeObject(schema);
	}
	
}

