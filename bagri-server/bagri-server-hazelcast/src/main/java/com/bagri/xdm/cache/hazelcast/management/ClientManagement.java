package com.bagri.xdm.cache.hazelcast.management;

import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;

/**
 * @author Denis Sukhoroslov
 *
 */
@ManagedResource(description="Clients Management MBean")
public class ClientManagement extends SchemaFeatureManagement {

	public ClientManagement(String schemaName) {
		super(schemaName);
	}

	@Override
	protected String getFeatureKind() {
		return "ClientManagement";
	}

	@ManagedAttribute(description="Returns active clients count")
	public Integer getClientCount() {
		return 0; 
	}

}
