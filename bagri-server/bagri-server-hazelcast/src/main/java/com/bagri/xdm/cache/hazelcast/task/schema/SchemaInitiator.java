package com.bagri.xdm.cache.hazelcast.task.schema;

import static com.bagri.xdm.cache.hazelcast.serialize.DataSerializationFactoryImpl.cli_InitSchemaTask;
import static com.bagri.xdm.client.hazelcast.serialize.DataSerializationFactoryImpl.factoryId;
import static com.bagri.xdm.common.XDMConstants.xdm_schema_name;
import static com.bagri.xdm.cache.hazelcast.util.SpringContextHolder.*;
import static com.bagri.xdm.cache.hazelcast.util.HazelcastUtils.hz_instance;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.env.PropertiesPropertySource;

import com.bagri.xdm.cache.api.SchemaRepository;
import com.bagri.xdm.cache.hazelcast.impl.PopulationManagementImpl;
import com.bagri.xdm.cache.hazelcast.impl.RepositoryImpl;
import com.bagri.xdm.system.Schema;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;
import com.hazelcast.spring.context.SpringAware;

@SpringAware
public class SchemaInitiator implements Callable<Boolean>, IdentifiedDataSerializable { //extends SchemaDenitiator {

	protected final transient Logger logger = LoggerFactory.getLogger(SchemaInitiator.class);
	private Schema schema;
	
	public SchemaInitiator() {
		//
	}

	public SchemaInitiator(Schema schema) {
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

    		hz = ctx.getBean(hz_instance, HazelcastInstance.class);
    		//hz.getConfig().getSecurityConfig().setEnabled(true);
			setContext(schemaName, schema_context, ctx);
			
			RepositoryImpl xdmRepo = ctx.getBean(SchemaRepository.bean_id, RepositoryImpl.class);
			xdmRepo.setSchema(schema);
    		logger.debug("initSchema; schema {} started on instance: {}", schemaName, hz);
    		
    		//PopulationManagementImpl popManager = ctx.getBean("popManager", PopulationManagementImpl.class);
    		PopulationManagementImpl popManager = (PopulationManagementImpl) hz.getUserContext().get("popManager");
    		if (popManager != null) {
    			popManager.checkPopulation(hz.getCluster().getMembers().size());
    		} else {
    			logger.debug("initSchema.exit; population for schema {} is disabled", schemaName);
    		}
    		
    		return true;
    	} catch (Exception ex) {
    		logger.error("initSchema.error; " + ex.getMessage(), ex);
    		return false;
    	}
	}
	
	@Override
	public int getId() {
		return cli_InitSchemaTask;
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

