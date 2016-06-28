package com.bagri.xdm.cache.hazelcast.management;

import static com.bagri.xquery.api.XQUtils.getTypeName;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.management.openmbean.TabularData;

import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;

import com.bagri.xdm.api.XDMException;
import com.bagri.xdm.api.impl.ModelManagementBase;
import com.bagri.xdm.cache.hazelcast.task.index.IndexCreator;
import com.bagri.xdm.cache.hazelcast.task.index.IndexRemover;
import com.bagri.xdm.cache.hazelcast.task.model.ModelRegistrator;
import com.bagri.xdm.domain.DocumentType;
import com.bagri.xdm.domain.Namespace;
import com.bagri.xdm.domain.Path;
import com.bagri.xdm.system.Fragment;
import com.bagri.xdm.system.Index;
import com.bagri.xdm.system.Schema;
import com.hazelcast.core.Member;

@ManagedResource(description="Model Management MBean")
public class ModelManagement extends SchemaFeatureManagement {

    public ModelManagement(String schemaName) {
    	super(schemaName);
    }

	@Override
	protected String getFeatureKind() {
		return "ModelManagement";
	}
	
	@Override
	protected Collection getSchemaFeatures(Schema schema) {
		return schema.getFragments();
	}

	@ManagedAttribute(description="Return Document Types registered in the Schema")
	public String[] getDocumentTypes() {
		Collection<DocumentType> types = ((ModelManagementBase) modelMgr).getDocumentTypes();
		String[] result = new String[types.size()];
		int idx = 0;
		for (DocumentType type: types) {
			result[idx++] = "" + type.getTypeId() + ": " + type.getRootPath();
		}
		Arrays.sort(result);
		return result;
	}

	@ManagedAttribute(description="Return Fragments registered in the Schema")
	public TabularData getFragments() {
		return getTabularFeatures("fragment", "Fragment definition", "name");
	}
	
	@ManagedAttribute(description="Return Namespaces registered in the Schema")
	public String[] getNamespaces() {
		Collection<Namespace> nss = ((ModelManagementBase) modelMgr).getNamespaces();
		String[] result = new String[nss.size()];
		int idx = 0;
		for (Namespace ns: nss) {
			result[idx++] = ns.getPrefix() + ": " + ns.getUri();
		}
		Arrays.sort(result);
		return result;
	}
	
	@ManagedOperation(description="Adds a new Document Fragment")
	@ManagedOperationParameters({
		@ManagedOperationParameter(name = "name", description = "Fragment name to create"),
		@ManagedOperationParameter(name = "docType", description = "Root path for document type"),
		@ManagedOperationParameter(name = "path", description = "XPath to Fragment"),
		@ManagedOperationParameter(name = "description", description = "Fragment description")})
	public void addFragment(String name, String docType, String path, String description) {

		logger.trace("addFragment.enter;");
		long stamp = System.currentTimeMillis();
		Fragment fragment = schemaManager.addFragment(name, docType, path, description);
		if (fragment == null) {
			throw new IllegalStateException("Fragment '" + name + "' in schema '" + schemaName + "' already exists");
		}
		
		int cnt = 0;
		stamp = System.currentTimeMillis() - stamp;
		logger.trace("addFragment.exit; fragment added on {} members; timeTaken: {}", cnt, stamp);
	}
	
	@ManagedOperation(description="Removes an existing Fragment")
	@ManagedOperationParameters({@ManagedOperationParameter(name = "name", description = "Fragment name to remove")})
	public void removeFragment(String name) {
		
		logger.trace("removeFragment.enter;");
		long stamp = System.currentTimeMillis();
		if (!schemaManager.deleteFragment(name)) {
			throw new IllegalStateException("Fragment '" + name + "' in schema '" + schemaName + "' does not exist");
		}

		int cnt = 0;
		stamp = System.currentTimeMillis() - stamp;
		logger.trace("removeFragment.exit; fragment deleted on {} members; timeTaken: {}", cnt, stamp);
	}

	@ManagedOperation(description="Enables/Disables an existing Fragment")
	@ManagedOperationParameters({
		@ManagedOperationParameter(name = "name", description = "Fragment name to enable/disable"),
		@ManagedOperationParameter(name = "enable", description = "enable/disable fragment")})
	public void enableFragment(String name, boolean enable) {
		
		if (!schemaManager.enableFragment(name, enable)) {
			throw new IllegalStateException("Fragment '" + name + "' in schema '" + schemaName + 
					"' does not exist or already " + (enable ? "enabled" : "disabled"));
		}
	}
	
	@ManagedOperation(description="Return all unique paths for the document type provided")
	@ManagedOperationParameters({
		@ManagedOperationParameter(name = "typeId", description = "A document type identifier")})
	public String[] getPathsForType(int typeId) {
		Collection<Path> paths = modelMgr.getTypePaths(typeId);
		String[] result = new String[paths.size()];
		int idx = 0;
		for (Path path: paths) {
			result[idx++] = "" + path.getPathId() + ": " + path.getPath() + 
					" (" + path.getNodeKind() + ":" + getTypeName(path.getDataType()) + ")";
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
		//int size = ((XDMModelManagementBase) modelMgr).getDocumentTypes().size();
		//try {
		//	modelMgr.registerSchemaUri(schemaFile);
		//	return ((XDMModelManagementBase) modelMgr).getDocumentTypes().size() - size;
		//} catch (XDMException ex) {
		//	logger.error("registerSchema.error:", ex);
		//}
		//return 0;
		
		logger.trace("registerSchema.enter;");
		long stamp = System.currentTimeMillis();
		ModelRegistrator task = new ModelRegistrator(schemaFile);
		Future<Integer> result = execService.submit(task);
		int cnt = 0;
		try {
			cnt = result.get();
		} catch (InterruptedException | ExecutionException ex) {
			logger.error("", ex);
		}
		stamp = System.currentTimeMillis() - stamp;
		logger.trace("registerSchema.exit; returning: {}; timeTaken: {}", cnt, stamp);
		return cnt;
	}
	
	@ManagedOperation(description="Register Schemas")
	@ManagedOperationParameters({
		@ManagedOperationParameter(name = "schemaCatalog", description = "A full path to the directory containing XSD files to register")})
	public int registerSchemas(String schemasCatalog) {
		int size = ((ModelManagementBase) modelMgr).getDocumentTypes().size(); 
		try {
			((ModelManagementBase) modelMgr).registerSchemas(schemasCatalog);
			return ((ModelManagementBase) modelMgr).getDocumentTypes().size() - size;
		} catch (XDMException ex) {
			logger.error("registerSchemas.error:", ex);
		}
		return 0;
	}
	
	
}
