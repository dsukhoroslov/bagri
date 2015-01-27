package com.bagri.xdm.cache.hazelcast.management;

import static com.bagri.xdm.client.common.XDMCacheConstants.PN_XDM_SYSTEM_POOL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.xdm.cache.hazelcast.task.schema.SchemaPopulator;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.MemberAttributeEvent;
import com.hazelcast.core.MembershipEvent;
import com.hazelcast.core.MembershipListener;
import com.hazelcast.core.MigrationEvent;
import com.hazelcast.core.MigrationListener;
import com.hazelcast.spring.context.SpringAware;

@SpringAware
public class PopulationManager implements MembershipListener, MigrationListener { //, NodeAware, HazelcastInstanceAware {

    private static final transient Logger logger = LoggerFactory.getLogger(PopulationManager.class);

    private String schemaName;
    private int populationSize;
    private HazelcastInstance hzInstance;
    
    public PopulationManager(HazelcastInstance hzInstance) {
    	this.hzInstance = hzInstance;
    	hzInstance.getCluster().addMembershipListener(this);
    	hzInstance.getPartitionService().addMigrationListener(this);
    	
    	//ManagedContext ctx = hzInstance.getConfig().getManagedContext();
    	//logger.debug("<init>; HZ: {}; Context: {}", hzInstance, ctx);
    	//if (ctx != null) {
    	//	ctx.initialize(this);
        //	logger.debug("<init>; Node initialized: {}", node);
    	//}
    	//hzInstance = Hazelcast.getHazelcastInstanceByName(hzInstance.getName());
    	//logger.debug("<init>; second HZ: {}; Class: {}", hzInstance, hzInstance.getClass().getName());
    }

    public void setSchemaName(String schemaName) {
    	this.schemaName = schemaName;
    }
    
    public void setPopulationSize(int populationSize) {
    	this.populationSize = populationSize;
    }
    
    public void checkPopulation(int currentSize) {
    	if (populationSize == currentSize) {
    		logger.debug("checkPopulation; starting population on cluster size: {}", currentSize);
    		SchemaPopulator pop = new SchemaPopulator(schemaName);
    		hzInstance.getExecutorService(PN_XDM_SYSTEM_POOL).submitToMember(pop, hzInstance.getCluster().getLocalMember());
    	} else {
    		logger.debug("checkPopulation; cluster size ({}) does not match configured population size ({}), skipping population",
    				currentSize, populationSize);
    	}
    }
	
	@Override
	public void memberAdded(MembershipEvent membershipEvent) {
		logger.trace("memberAdded; event: {}", membershipEvent);
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

}
