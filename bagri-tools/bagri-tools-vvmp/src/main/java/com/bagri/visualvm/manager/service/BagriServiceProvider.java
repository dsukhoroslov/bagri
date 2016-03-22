package com.bagri.visualvm.manager.service;

public interface BagriServiceProvider {

	ClusterManagementService getClusterManagement();
	
	SchemaManagementService getSchemaManagement();
	
	UserManagementService getUserManagement();
	
}
