package com.bagri.server.hazelcast.task.schema;

import static com.bagri.core.server.api.CacheConstants.*;
import static com.bagri.server.hazelcast.serialize.DataSerializationFactoryImpl.cli_CleanSchemaTask;
import static com.bagri.server.hazelcast.util.HazelcastUtils.findSchemaInstance;

import java.io.IOException;
import java.util.concurrent.Callable;

import org.springframework.beans.factory.annotation.Autowired;

import com.bagri.core.model.Query;
import com.bagri.server.hazelcast.impl.HealthManagementImpl;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.ReplicatedMap;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.spring.context.SpringAware;

@SpringAware
public class SchemaDocCleaner extends SchemaProcessingTask implements Callable<Boolean> {
	
	private boolean evictOnly;
	private HealthManagementImpl hMgr;
	
	public SchemaDocCleaner() {
		super();
	}

	public SchemaDocCleaner(String schemaName, boolean evictOnly) {
		super(schemaName);
		this.evictOnly = evictOnly;
	}
	
    @Autowired
	public void setHealthManagement(HealthManagementImpl hMgr) {
		this.hMgr = hMgr;
	}

	@Override
	public Boolean call() throws Exception {
    	logger.trace("call.enter; schema: {}", schemaName);
    	boolean result = false;
		HazelcastInstance hz = findSchemaInstance(schemaName);
		if (hz != null) {
			// get docs caches and clean them
			cleanCache(hz, CN_XDM_CONTENT);
			cleanCache(hz, CN_XDM_DOCUMENT);
			cleanCache(hz, CN_XDM_ELEMENT);
			cleanCache(hz, CN_XDM_INDEX);
			cleanCache(hz, CN_XDM_RESULT);
			//cleanCache(hz, CN_XDM_QUERY);
		    ReplicatedMap<Integer, Query> xqCache = hz.getReplicatedMap(CN_XDM_QUERY);
		    xqCache.clear();
			hMgr.clearState();
			System.gc();
			result = true;
		}
    	logger.trace("call.exit; schema {} cleaned: {}", schemaName, result);
		return result;
	}
	
	private void cleanCache(HazelcastInstance hz, String cacheName) {
		IMap cache = hz.getMap(cacheName);
		if (evictOnly) {
			cache.evictAll();
		} else {
			cache.clear();
		}
	}

	@Override
	public int getId() {
		return cli_CleanSchemaTask;
	}

	@Override
	public void readData(ObjectDataInput in) throws IOException {
		super.readData(in);
		evictOnly = in.readBoolean();
	}
	
	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		super.writeData(out);
		out.writeBoolean(evictOnly);
	}
}
