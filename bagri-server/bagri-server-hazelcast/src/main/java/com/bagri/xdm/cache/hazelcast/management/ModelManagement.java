package com.bagri.xdm.cache.hazelcast.management;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import javax.management.openmbean.TabularData;

import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;

import com.bagri.xdm.client.common.impl.XDMModelManagementBase;
import com.bagri.xdm.domain.XDMDocumentType;

@ManagedResource(description="Model Management MBean")
public class ModelManagement extends SchemaFeatureManagement {

    public ModelManagement(String schemaName) {
    	super(schemaName);
    }

	@Override
	protected String getFeatureKind() {
		return "ModelManagement";
	}

	@ManagedAttribute(description="Returns Document Types registered in the Schema")
	public String[] getRegisteredTypes() {
		Collection<XDMDocumentType> types = ((XDMModelManagementBase) modelMgr).getDocumentTypes();
		String[] result = new String[types.size()];
		Iterator<XDMDocumentType> itr = types.iterator();
		for (int i=0; i < types.size(); i++) {
			result[i] = itr.next().getRootPath();
		}
		Arrays.sort(result);
		return result;
	}

	@ManagedAttribute(description="Return candidate index usage statistics, per query path")
	public TabularData getPathStatistics() {
		return null;
	}
	
	@ManagedOperation(description="Register Schema")
	@ManagedOperationParameters({
		@ManagedOperationParameter(name = "schemaFile", description = "A full path to XSD file to register")})
	public int registerSchema(String schemaFile) {
		int size = ((XDMModelManagementBase) modelMgr).getDocumentTypes().size(); 
		modelMgr.registerSchemaUri(schemaFile);
		return ((XDMModelManagementBase) modelMgr).getDocumentTypes().size() - size;
	}
	
	@ManagedOperation(description="Register Schemas")
	@ManagedOperationParameters({
		@ManagedOperationParameter(name = "schemaCatalog", description = "A full path to the directory containing XSD files to register")})
	public int registerSchemas(String schemasCatalog) {
		int size = ((XDMModelManagementBase) modelMgr).getDocumentTypes().size(); 
		((XDMModelManagementBase) modelMgr).registerSchemas(schemasCatalog);
		return ((XDMModelManagementBase) modelMgr).getDocumentTypes().size() - size;
	}
	
	
}
