package com.bagri.visualvm.manager.service;

public interface BagriServiceProvider {
	
	void close();

	ClusterManagementService getClusterManagement();
	
	SchemaManagementService getSchemaManagement();
	
	UserManagementService getUserManagement();
	
}
