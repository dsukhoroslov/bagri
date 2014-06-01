package com.bagri.xdm.process.hazelcast.schema;

import static com.bagri.xdm.access.api.XDMCacheConstants.CN_XDM_DOCUMENT;
import static com.bagri.xdm.access.api.XDMCacheConstants.CN_XDM_ELEMENT;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.bagri.xdm.common.XDMDataKey;
import com.bagri.xdm.domain.XDMDocument;
import com.bagri.xdm.domain.XDMElement;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.monitor.LocalMapStats;
import com.hazelcast.spring.context.SpringAware;

@SpringAware
public class SchemaStatsAggregator extends com.bagri.xdm.access.hazelcast.process.SchemaStatsAggregator {
	
	private static final transient Logger logger = LoggerFactory.getLogger(SchemaStatsAggregator.class);
    
	private transient HazelcastInstance hzInstance;
    
    @Autowired
	public void setHzInstance(HazelcastInstance hzInstance) {
		this.hzInstance = hzInstance;
		logger.debug("sethzInstance; got instance: {}", hzInstance); 
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
    
}
