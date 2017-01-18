package com.bagri.xdm.cache.coherence.process;

import com.bagri.xdm.cache.coherence.store.AbstractCacheStore;
import com.tangosol.net.GuardSupport;
import com.tangosol.net.NamedCache;
import com.tangosol.net.cache.CacheEvent;
import com.tangosol.util.MapEvent;
import com.tangosol.util.MapListener;
import com.tangosol.util.MapTrigger;
import com.tangosol.util.MultiplexingMapListener;
import com.tangosol.util.SimpleMapEntry;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Replicated Cache Populator class
 */
public class ReplicatedCachePopulator extends StatsCollectingPopulator {

    private static final long serialVersionUID = 6396925457784124817L;

    /**
     * Class constructor
     *
     * @param cacheName Cache name
     */
    public ReplicatedCachePopulator(String cacheName) {
        super(cacheName);
    }
    
    @Override
    protected MapListener getMapTriggerListener(MapTrigger t) {

    	// the trigger registered in this way does not work on replicated cache
    	// as the plain MapTriggerListener just throws java.lang.IllegalStateException: 
    	// MapTriggerListener may not be used as a generic MapListener
    	// at com.tangosol.util.MapTriggerListener.onMapEvent(MapTriggerListener.java:55)
    	//return new MapTriggerListener(t);
    	
    	return new ReplicatedTriggerListener(t);
    }
    
    
    /**
     * @param cache Named cache
     */
    @Override
    protected void populateCache(NamedCache cache) {
        final AbstractCacheStore store = getStore();
        if (store.isSupportBatchLoading()) {
            loadBatches(cache);
        } else {
            loadAllAtOnce(cache);
        }

        //stamp = System.currentTimeMillis() - stamp;
        //if (store instanceof SpotRateCacheStore) {
        //    getRFM().addStatisticSeries(Calendar.getInstance().getTime(), true, stamp, cache.size());
        //}
        //log.info("Cache {} populated; time taken: {}", cache.getCacheName(), stamp);
    }

    protected void loadBatches(NamedCache cache) {
        AbstractCacheStore store = getStore();
        List allKeys = store.getDataKeys();
    	keys = allKeys.size();
    	log.debug("Got {} keys to populate", keys);
        int idx = 0;
        while (idx < keys) {
            GuardSupport.heartbeat();
            int next = Math.min(idx + getBatchSize(), keys);
            queried = next;
            Map data = store.loadAll(allKeys.subList(idx, next));
			loaded += data.size();
            cache.putAll(data);
            log.debug("Got {} entities populated", data.size());
            idx += getBatchSize();
			batches++;
        }
    }

    protected void loadAllAtOnce(NamedCache cache) {
        GuardSupport.heartbeat();
        AbstractCacheStore store = getStore();
        List allKeys = store.getDataKeys();
    	keys = allKeys.size();
        Map data = store.loadAll(allKeys);
        queried = keys;
		loaded = data.size();
        cache.putAll(data);
        log.debug("Got {} entities populated", data.size());
		batches = 1;
    }


    //private RatesFeedManagerBean getRFM() {
    //    RatesFeedManagerBean rfm = SpringAwareCacheFactory.getBeanOrThrowException("rfManager", RatesFeedManagerBean.class);
    //    log.trace("getRFM: TPM: {}", rfm);
    //    return rfm;
    //}


    public static class ReplicatedTriggerListener extends MultiplexingMapListener {
    	
        private static final Logger LOG = LoggerFactory.getLogger(ReplicatedTriggerListener.class);
    	
    	private MapTrigger trigger;
    	
    	ReplicatedTriggerListener(MapTrigger trigger) {
    		this.trigger = trigger;
    	}
    	
    	// this custom trigger does not work properly: on the node where the change happened
    	// oldValue is equal newValue for some reason. On other nodes old values are correct!
    	
		@Override
		public void onMapEvent(MapEvent event) {

            LOG.trace("got event: {}, type: {}", event, event.getClass().getName());
            LOG.trace("got entry: {} and source: {}", event.getOldEntry(), event.getSource());
			
	        if (event instanceof CacheEvent && ((CacheEvent) event).isSynthetic()) {
	            //LOG.trace("Skipping synthetic event: {}", event);
	            return;
	        }

	        //if (!context.isKeyOwned(evt.getKey())) {
	        //    LOGGER.trace("Skipping redistribution event: {}", evt);
	        //    return;
	        //}
			
			trigger.process(new SimpleMapEntry(event.getKey(), event.getNewValue(), event.getOldValue()));
		}

    }

}
