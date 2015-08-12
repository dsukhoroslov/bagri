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

import com.bagri.xdm.api.XDMException;
import com.bagri.xdm.client.common.impl.XDMModelManagementBase;
import com.bagri.xdm.domain.XDMDocumentType;
import com.bagri.xdm.domain.XDMNamespace;
import com.bagri.xdm.domain.XDMPath;

@ManagedResource(description="Model Management MBean")
public class ModelManagement extends SchemaFeatureManagement {

    public ModelManagement(String schemaName) {
    	super(schemaName);
    }

	@Override
	protected String getFeatureKind() {
		return "ModelManagement";
	}

	@ManagedAttribute(description="Return Document Types registered in the Schema")
	public String[] getDocumentTypes() {
		Collection<XDMDocumentType> types = ((XDMModelManagementBase) modelMgr).getDocumentTypes();
		String[] result = new String[types.size()];
		int idx = 0;
		for (XDMDocumentType type: types) {
			result[idx++] = "" + type.getTypeId() + ": " + type.getRootPath();
		}
		Arrays.sort(result);
		return result;
	}

	@ManagedAttribute(description="Return Namespaces registered in the Schema")
	public String[] getNamespaces() {
		Collection<XDMNamespace> nss = ((XDMModelManagementBase) modelMgr).getNamespaces();
		String[] result = new String[nss.size()];
		int idx = 0;
		for (XDMNamespace ns: nss) {
			result[idx++] = ns.getPrefix() + ": " + ns.getUri();
		}
		Arrays.sort(result);
		return result;
	}
	
	@ManagedOperation(description="Return all unique paths for the document type provided")
	@ManagedOperationParameters({
		@ManagedOperationParameter(name = "typeId", description = "A document type identifier")})
	public String[] getPathsForType(int typeId) {
		Collection<XDMPath> paths = modelMgr.getTypePaths(typeId);
		String[] result = new String[paths.size()];
		int idx = 0;
		for (XDMPath path: paths) {
			result[idx++] = "" + path.getPathId() + ": " + path.getPath() + 
					" (" + path.getNodeKind() + ")";
		}
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
		try {
			modelMgr.registerSchemaUri(schemaFile);
			return ((XDMModelManagementBase) modelMgr).getDocumentTypes().size() - size;
		} catch (XDMException ex) {
			logger.error("registerSchema.error:", ex);
		}
		return 0;
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
