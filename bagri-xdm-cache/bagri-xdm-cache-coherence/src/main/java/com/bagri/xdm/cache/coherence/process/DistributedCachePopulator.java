package com.bagri.xdm.cache.coherence.process;

import com.tangosol.net.CacheService;
import com.tangosol.net.GuardSupport;
import com.tangosol.net.Member;
import com.tangosol.net.NamedCache;
import com.tangosol.net.PartitionedService;
import com.tangosol.util.MapListener;
import com.tangosol.util.MapTrigger;
import com.tangosol.util.MapTriggerListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Distributed Cache Populator class
 */
public class DistributedCachePopulator extends StatsCollectingPopulator {

    private static final long serialVersionUID = -7900392253620131375L;

    /**
     *
     * @param cacheName Cache name
     */
    public DistributedCachePopulator(String cacheName) {
		super(cacheName);
	}

    @Override
    protected MapListener getMapTriggerListener(MapTrigger t) {
    	return new MapTriggerListener(t);
    }
    
    /**
     *
     * @param cache Named cache
     */
    @Override
	protected void populateCache(NamedCache cache) {
        List allKeys = getStore().getDataKeys();
        if (allKeys != null && allKeys.size() > 0) {
        	keys = allKeys.size();
        	log.debug("Got {} keys to populate", keys);
        	CacheService svc = cache.getCacheService(); 
        	Member local = svc.getCluster().getLocalMember();
        	PartitionedService psvc = (PartitionedService) svc;
        	List keys = new ArrayList(getBatchSize());
        	for (Object key : allKeys) {
        		if (psvc.getKeyOwner(key) == local) {
        			keys.add(key);
        			if (keys.size() == getBatchSize()) {
                        GuardSupport.heartbeat();
                        queried += keys.size();
        				Map result = cache.getAll(keys);
        				loaded += result.size();
        				log.debug("Got {} entities populated", result.size());
        				keys.clear();
        				batches++;
        			}
        		}
        	}
        	
        	if (keys.size() > 0) {
                queried += keys.size();
				Map result = cache.getAll(keys);
				loaded += result.size();
				log.debug("Got {} entities populated", result.size());
				batches++;
			}
            log.debug("cache size after population: {}", cache.size());
        }
        
        //if (cacheName.equals("entity-statuses")) {
        //	com.tangosol.util.ValueExtractor ext = new com.tangosol.util.extractor.PofExtractor(long.class, 3);
    	//	cache.addIndex(ext, true, null);
		//	log.debug("applied custom index: {}", ext);
        //} else {
	}

    
}
