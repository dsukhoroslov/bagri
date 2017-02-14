package com.bagri.server.hazelcast.task.schema;

import static com.bagri.client.hazelcast.serialize.DataSerializationFactoryImpl.factoryId;
import static com.bagri.core.Constants.ctx_popService;
import static com.bagri.core.Constants.pn_config_path;
import static com.bagri.core.Constants.pn_schema_name;
import static com.bagri.server.hazelcast.serialize.DataSerializationFactoryImpl.cli_InitSchemaTask;
import static com.bagri.server.hazelcast.util.HazelcastUtils.hz_instance;
import static com.bagri.server.hazelcast.util.SpringContextHolder.*;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.env.PropertiesPropertySource;

import com.bagri.core.server.api.SchemaRepository;
import com.bagri.core.system.Schema;
import com.bagri.server.hazelcast.impl.PopulationManagementImpl;
import com.bagri.server.hazelcast.impl.SchemaRepositoryImpl;
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
		String schemaName = schema.getName();
		HazelcastInstance hz = Hazelcast.getHazelcastInstanceByName(schemaName);
		if (hz != null) {
    		logger.debug("initSchema.exit; schema {} already started on instance: {}, returning", schemaName, hz);
    		return false;
		}
		
		Properties props = schema.getProperties();
    	props.setProperty(pn_schema_name, schemaName);
    	PropertiesPropertySource pps = new PropertiesPropertySource(schemaName, props);
    	
    	try {
    		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext();
    		ctx.getEnvironment().getPropertySources().addFirst(pps);
            String contextPath = System.getProperty(pn_config_path);
    		ctx.setConfigLocation("file:" + contextPath + "/spring/cache-xqj-context.xml");
    		ctx.refresh();

    		hz = ctx.getBean(hz_instance, HazelcastInstance.class);
    		//hz.getConfig().getSecurityConfig().setEnabled(true);
			setContext(schemaName, ctx);
			
			SchemaRepositoryImpl xdmRepo = ctx.getBean(SchemaRepository.bean_id, SchemaRepositoryImpl.class);
			xdmRepo.setSchema(schema);
    		logger.debug("initSchema; schema {} started on instance: {}", schemaName, hz);
    		
    		PopulationManagementImpl popManager = (PopulationManagementImpl) hz.getUserContext().get(ctx_popService);
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

