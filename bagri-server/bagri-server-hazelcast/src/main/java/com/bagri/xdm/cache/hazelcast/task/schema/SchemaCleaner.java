package com.bagri.xdm.cache.hazelcast.task.schema;

import static com.bagri.xdm.client.common.XDMCacheConstants.*;
import static com.bagri.xdm.client.hazelcast.serialize.XDMDataSerializationFactory.cli_XDMCleanSchemaTask;

import java.io.IOException;
import java.util.concurrent.Callable;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;

public class SchemaCleaner extends SchemaProcessingTask implements Callable<Boolean> {
	
	private boolean evictOnly;
	
	public SchemaCleaner() {
		super();
	}

	public SchemaCleaner(String schemaName, boolean evictOnly) {
		super(schemaName);
		this.evictOnly = evictOnly;
	}

	@Override
	public Boolean call() throws Exception {
    	logger.trace("call.enter; schema: {}", schemaName);
    	boolean result = false;
		HazelcastInstance hz = Hazelcast.getHazelcastInstanceByName(schemaName);
		if (hz != null) {
			// get docs caches and clean them
			cleanCache(hz, CN_XDM_XML);
			cleanCache(hz, CN_XDM_DOCUMENT);
			cleanCache(hz, CN_XDM_ELEMENT);
			cleanCache(hz, CN_XDM_INDEX);
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
		return cli_XDMCleanSchemaTask;
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
