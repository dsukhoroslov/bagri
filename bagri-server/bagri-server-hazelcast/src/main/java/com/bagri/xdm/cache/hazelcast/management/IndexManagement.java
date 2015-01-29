package com.bagri.xdm.cache.hazelcast.management;

import java.util.Map;
import java.util.Set;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.TabularData;

import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;

import com.bagri.common.manage.JMXUtils;
import com.bagri.xdm.system.XDMIndex;
import com.bagri.xdm.system.XDMSchema;
import com.hazelcast.core.IMap;

@ManagedResource(description="Schema Indexes Management MBean")
public class IndexManagement extends SchemaFeatureManagement {
	
	private IMap<String, XDMSchema> schemaCache;

	public IndexManagement(String schemaName) {
		super(schemaName);
	}

	protected String getFeatureKind() {
		return "IndexManagement";
	}
	
	public void setSchemaCache(IMap<String, XDMSchema> schemaCache) {
		this.schemaCache = schemaCache;
	}

	@ManagedAttribute(description="Return indexes defined on Schema")
	public TabularData getIndexes() {
		// get XDMSchema somehow! then get its indexes and build TabularData
		XDMSchema schema = schemaCache.get(schemaName);
		Set<XDMIndex> indexes = schema.getIndexes();
		if (indexes.size() == 0) {
			return null;
		}
		
        TabularData result = null;
        for (XDMIndex index: indexes) {
            try {
                Map<String, Object> def = index.toMap();
                CompositeData data = JMXUtils.mapToComposite("index", "Index definition", def);
                result = JMXUtils.compositeToTabular("index", "Index definition", "name", result, data);
            } catch (Exception ex) {
                logger.error("getIndexes; error", ex);
            }
        }
        return result;
    }
	
	@ManagedAttribute(description="Return aggregated index usage statistics, per index")
	public TabularData getIndexStatistics() {
		return null;
	}

	@ManagedOperation(description="Creates a new Index")
	@ManagedOperationParameters({@ManagedOperationParameter(name = "path", description = "XPath to index")})
	public void addIndex(String path) {
		// not implemented yet
	}
	
	@ManagedOperation(description="Removes an existing Index")
	@ManagedOperationParameters({@ManagedOperationParameter(name = "path", description = "XPath to delete from indexes")})
	public void dropIndex(String path) {
		// not implemented yet
	}

	@ManagedOperation(description="Rebuilds an existing Index")
	@ManagedOperationParameters({@ManagedOperationParameter(name = "path", description = "XPath to rebuild")})
	public void rebuildIndex(String path) {
		// not implemented yet
	}

}
