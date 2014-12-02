package com.bagri.xdm.cache.hazelcast.management;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.jmx.export.naming.SelfNaming;

import com.bagri.common.manage.JMXUtils;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.core.MemberAttributeEvent;
import com.hazelcast.core.MembershipEvent;
import com.hazelcast.core.MembershipListener;

@ManagedResource(description="Population Manager MBean")
public class PopulationManagement implements SelfNaming {

    private String schemaName;
	private HazelcastInstance hzInstance;
	private IExecutorService execService;
	
    
    public PopulationManagement(String schemaName) {
    	this.schemaName = schemaName;
	}
    
    public void setHzInstance(HazelcastInstance hzInstance) {
		this.hzInstance = hzInstance;
    }
	
	@Override
	public ObjectName getObjectName() throws MalformedObjectNameException {
		return JMXUtils.getObjectName("type=" + "Schema" + ",name=" + schemaName + ",kind=PopulationManagement");
	}


}
