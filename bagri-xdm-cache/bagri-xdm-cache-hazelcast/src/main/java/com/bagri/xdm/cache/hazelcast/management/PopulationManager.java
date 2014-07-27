package com.bagri.xdm.cache.hazelcast.management;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import com.bagri.xdm.process.hazelcast.schema.SchemaPopulator;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.MemberAttributeEvent;
import com.hazelcast.core.MembershipEvent;
import com.hazelcast.core.MembershipListener;

public class PopulationManager implements MembershipListener {

    private static final transient Logger logger = LoggerFactory.getLogger(PopulationManager.class);
    
    private String schemaName;
    private int populationSize;
    private HazelcastInstance hzInstance;
    
    public PopulationManager(HazelcastInstance hzInstance) {
    	this.hzInstance = hzInstance;
    	//hzInstance.getCluster().addMembershipListener(this);
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
    		hzInstance.getExecutorService("xdm-exec-pool").submitToMember(pop, hzInstance.getCluster().getLocalMember());
    	} else {
    		logger.debug("checkPopulation; cluster size ({}) does not conform to configured size ({}), skipping population",
    				currentSize, populationSize);
    	}
    }
	
	@Override
	public void memberAdded(MembershipEvent membershipEvent) {
		logger.trace("memberAdded; event: {}", membershipEvent);
		if (membershipEvent.getMember().localMember()) {
			checkPopulation(membershipEvent.getMembers().size());
		}
	}

	@Override
	public void memberRemoved(MembershipEvent membershipEvent) {
		logger.trace("memberRemoved; event: {}", membershipEvent);
	}

	@Override
	public void memberAttributeChanged(MemberAttributeEvent memberAttributeEvent) {
		logger.trace("memberAttributeChaged; event: {}", memberAttributeEvent);
	}


}
