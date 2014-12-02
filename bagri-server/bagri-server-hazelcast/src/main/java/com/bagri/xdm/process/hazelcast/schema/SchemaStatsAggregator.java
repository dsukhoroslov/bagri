package com.bagri.xdm.process.hazelcast.schema;

import static com.bagri.xdm.access.api.XDMCacheConstants.CN_XDM_DOCUMENT;
import static com.bagri.xdm.access.api.XDMCacheConstants.CN_XDM_ELEMENT;
import static com.bagri.xdm.access.hazelcast.pof.XDMPortableFactory.cli_XDMSchemaAggregationTask;
import static com.bagri.xdm.access.hazelcast.pof.XDMPortableFactory.factoryId;

import java.io.IOException;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.bagri.xdm.common.XDMDataKey;
import com.bagri.xdm.domain.XDMDocument;
import com.bagri.xdm.domain.XDMElement;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.monitor.LocalMapStats;
import com.hazelcast.nio.serialization.Portable;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.hazelcast.spring.context.SpringAware;

@SpringAware
public class SchemaStatsAggregator implements Callable<Long>, Portable {
	
	private static final transient Logger logger = LoggerFactory.getLogger(SchemaStatsAggregator.class);
    
	private transient HazelcastInstance hzInstance;

	@Override
	public int getClassId() {
		// logger.trace("getClassId.exit; returning: {}", cli_XDMDocumentTask);
		return cli_XDMSchemaAggregationTask;
	}

	@Override
	public int getFactoryId() {
		// logger.trace("getFactoryId.exit; returning: {}", factoryId);
		return factoryId;
	}

	
    @Autowired
	public void setHzInstance(HazelcastInstance hzInstance) {
		this.hzInstance = hzInstance;
		logger.trace("setHzInstance; got instance: {}", hzInstance); 
	}
    
	@Override
	public Long call() throws Exception {
		
		IMap<Long, XDMDocument> xddCache = hzInstance.getMap(CN_XDM_DOCUMENT);
		IMap<XDMDataKey, XDMElement> xdmCache = hzInstance.getMap(CN_XDM_ELEMENT);
		
    	LocalMapStats stats = xddCache.getLocalMapStats();
    	long size = stats.getBackupEntryMemoryCost() + stats.getOwnedEntryMemoryCost();
    	stats = xdmCache.getLocalMapStats();
    	size += stats.getBackupEntryMemoryCost() + stats.getOwnedEntryMemoryCost();
        return size; 
    }
    
	@Override
	public void readPortable(PortableReader in) throws IOException {
		// logger.trace("readPortable.enter; in: {}", in);
	}

	@Override
	public void writePortable(PortableWriter out) throws IOException {
		// logger.trace("writePortable.enter; out: {}", out);
	}

}




