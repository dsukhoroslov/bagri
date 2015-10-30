package com.bagri.xdm.cache.hazelcast.impl;

import static com.bagri.xdm.client.common.XDMCacheConstants.PN_XDM_SCHEMA_POOL;
import static com.bagri.common.config.XDMConfigConstants.xdm_schema_name;
import static com.bagri.common.config.XDMConfigConstants.xdm_schema_population_size;

import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import com.bagri.xdm.cache.hazelcast.task.schema.SchemaPopulator;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.MemberAttributeEvent;
import com.hazelcast.core.MembershipEvent;
import com.hazelcast.core.MembershipListener;
import com.hazelcast.core.MigrationEvent;
import com.hazelcast.core.MigrationListener;
import com.hazelcast.partition.PartitionLostEvent;
import com.hazelcast.partition.PartitionLostListener;
import com.hazelcast.spi.ManagedService;
import com.hazelcast.spi.NodeEngine;

public class PopulationManagementImpl implements ManagedService, 
	MembershipListener, MigrationListener, PartitionLostListener { 

    private static final transient Logger logger = LoggerFactory.getLogger(PopulationManagementImpl.class);

    private String schemaName;
    private int populationSize;
    private NodeEngine nodeEngine;
    
	@Override
	public void init(NodeEngine nodeEngine, Properties properties) {
		logger.info("init; got properties: {}", properties); 
		this.nodeEngine = nodeEngine;
		this.schemaName = properties.getProperty(xdm_schema_name);
		this.populationSize = Integer.parseInt(properties.getProperty(xdm_schema_population_size));
		
		nodeEngine.getHazelcastInstance().getCluster().addMembershipListener(this);
		nodeEngine.getPartitionService().addMigrationListener(this);
		nodeEngine.getPartitionService().addPartitionLostListener(this);
		nodeEngine.getHazelcastInstance().getUserContext().put("popManager", this);
	}

	@Override
	public void reset() {
		logger.info("reset"); 
	}

	@Override
	public void shutdown(boolean terminate) {
		logger.info("shutdown; terminate: {}", terminate); 
	}


	public void checkPopulation(int currentSize) {
    	if (populationSize <= currentSize) {
    		logger.debug("checkPopulation; starting population on cluster size: {}", currentSize);
    		SchemaPopulator pop = new SchemaPopulator(schemaName);
    		nodeEngine.getHazelcastInstance().getExecutorService(PN_XDM_SCHEMA_POOL).submitToMember(pop, 
    				nodeEngine.getLocalMember());
    	} else {
    		logger.debug("checkPopulation; cluster size ({}) does not match configured population size ({}), skipping population",
    				currentSize, populationSize);
    	}
    }
	
	public ManagedService getHzService(String serviceName, String instanceName) {
		return nodeEngine.getHazelcastInstance().getDistributedObject(serviceName, instanceName);
	}
	
	@Override
	public void memberAdded(MembershipEvent membershipEvent) {
		logger.info("memberAdded; event: {}", membershipEvent);
		//if (membershipEvent.getMember().localMember()) {
			checkPopulation(membershipEvent.getMembers().size());
		//}
	}

	@Override
	public void memberRemoved(MembershipEvent membershipEvent) {
		logger.trace("memberRemoved; event: {}", membershipEvent);
	}

	@Override
	public void memberAttributeChanged(MemberAttributeEvent memberAttributeEvent) {
		logger.trace("memberAttributeChaged; event: {}", memberAttributeEvent);
	}

	@Override
	public void migrationStarted(MigrationEvent migrationEvent) {
		logger.trace("migrationStarted; event: {}", migrationEvent);
	}

	@Override
	public void migrationCompleted(MigrationEvent migrationEvent) {
		logger.trace("migrationCompleted; event: {}", migrationEvent);
	}

	@Override
	public void migrationFailed(MigrationEvent migrationEvent) {
		logger.trace("migrationFailed; event: {}", migrationEvent);
	}

	//@Override
	public void migrationInitialized(MigrationEvent migrationEvent) {
		logger.trace("migrationInitialized; event: {}", migrationEvent);
	}

	//@Override
	public void migrationFinalized(MigrationEvent migrationEvent) {
		logger.trace("migrationFinalized; event: {}", migrationEvent);
	}

	@Override
	public void partitionLost(PartitionLostEvent event) {
		logger.info("partitionLost; event: {}", event);
	}


}
