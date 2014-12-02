package com.bagri.xdm.cache.hazelcast.management;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.jmx.export.naming.SelfNaming;

import com.bagri.common.manage.JMXUtils;

@ManagedResource(description="Query Manager MBean")
public class QueryManager implements SelfNaming {
	
	private String uuid;
	private String schemaName;
	
	public QueryManager() {
		//
	}
	
	public QueryManager(String schemaName, String uuid) {
		this.schemaName = schemaName;
		this.uuid = uuid;
	}

	@Override
	public ObjectName getObjectName() throws MalformedObjectNameException {
		return JMXUtils.getObjectName("type=Schema,name=" + schemaName + ",kind=QueryManagement,node=" + uuid);
	}

}
