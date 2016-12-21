package com.bagri.server.hazelcast.task.schema;

import static com.bagri.server.hazelcast.serialize.DataSerializationFactoryImpl.cli_CleanQueryTask;

import java.util.concurrent.Callable;

import org.springframework.beans.factory.annotation.Autowired;

import com.bagri.core.server.api.QueryManagement;
import com.hazelcast.spring.context.SpringAware;

@SpringAware
public class SchemaQueryCleaner extends SchemaProcessingTask implements Callable<Boolean> {
	
	private QueryManagement xqManager;
	
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
	public void setQueryManagement(QueryManagement xqManager) {
		this.xqManager = xqManager;
	}

}
