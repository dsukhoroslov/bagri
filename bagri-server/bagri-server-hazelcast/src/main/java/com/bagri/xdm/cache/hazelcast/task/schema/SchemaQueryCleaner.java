package com.bagri.xdm.cache.hazelcast.task.schema;

import static com.bagri.xdm.cache.hazelcast.serialize.DataSerializationFactoryImpl.cli_CleanQueryTask;

import java.util.concurrent.Callable;

import org.springframework.beans.factory.annotation.Autowired;

import com.bagri.xdm.cache.api.XDMQueryManagement;
import com.hazelcast.spring.context.SpringAware;

@SpringAware
public class SchemaQueryCleaner extends SchemaProcessingTask implements Callable<Boolean> {
	
	private XDMQueryManagement xqManager;
	
	public SchemaQueryCleaner() {
		super();
	}

	public SchemaQueryCleaner(String schemaName) {
		super(schemaName);
	}

	@Override
	public Boolean call() throws Exception {
    	xqManager.clearCache();
		return true;
	}
	
	@Override
	public int getId() {
		return cli_CleanQueryTask;
	}

    @Autowired
	public void setQueryManagement(XDMQueryManagement xqManager) {
		this.xqManager = xqManager;
	}

}
