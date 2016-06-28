package com.bagri.tools.vvm.service;

public interface BagriServiceProvider {
	
	void close();

	ClusterManagementService getClusterManagement();
	
	SchemaManagementService getSchemaManagement();
	
	UserManagementService getUserManagement();
	
}
