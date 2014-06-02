package com.bagri.xdm.cache.hazelcast.management;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.jmx.export.naming.SelfNaming;

import com.bagri.common.manage.JMXUtils;

@ManagedResource(description="Document Manager MBean")
public class DocumentManager implements SelfNaming {

	private String uuid;
	private String schemaName;
	
	public DocumentManager() {
		//
	}
	
	public DocumentManager(String schemaName, String uuid) {
		this.schemaName = schemaName;
		this.uuid = uuid;
	}

	@Override
	public ObjectName getObjectName() throws MalformedObjectNameException {
		return JMXUtils.getObjectName("type=Schema,name=" + schemaName + ",kind=DocumentManagement,node=" + uuid);
	}
	
}
