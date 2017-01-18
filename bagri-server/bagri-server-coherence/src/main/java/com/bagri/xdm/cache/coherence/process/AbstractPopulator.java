package com.bagri.xdm.cache.coherence.process;

import com.bagri.xdm.cache.coherence.store.AbstractCacheStore;
import com.tangosol.net.AbstractInvocable;
import com.tangosol.net.BackingMapManager;
import com.tangosol.net.BackingMapManagerContext;
import com.tangosol.net.CacheFactory;
import com.tangosol.net.CacheService;
import com.tangosol.net.InvocationObserver;
import com.tangosol.net.Member;
import com.tangosol.net.NamedCache;
import com.tangosol.net.cache.ReadWriteBackingMap;
//import com.tangosol.util.MapListener;
import com.tangosol.util.MapListener;
import com.tangosol.util.MapTrigger;
import com.tangosol.util.MapTriggerListener;
import com.tangosol.util.ValueExtractor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * Abstract Populator class
 */
public abstract class AbstractPopulator extends AbstractInvocable implements InvocationObserver {

	protected final Logger log;

    private int batchSize = 10000; //default size

    private String cacheName;
	private List<ValueExtractor> indexes;
    private List<MapTrigger> triggers;

    private boolean populating;
    private  AbstractCacheStore store;
    //private MapListener listener;

    /**
     * Class constructor
     * @param cacheName Cache name
     */
    public AbstractPopulator(String cacheName) {
        this.cacheName = cacheName;
        log = LoggerFactory.getLogger(getClass().getName() + "[" + cacheName + "]");
    }

    /**
     *
     * @return Cache name
     */
    public String getCacheName() {
        return cacheName;
    }

    /**
     *
     * @return Cache store
     */
    public AbstractCacheStore getStore() {
        return store;
    }

    /**
     *
     * @return Named cache
     */
    protected NamedCache getCache() {
        return CacheFactory.getCache(cacheName); 
    }

    /**
    *
    * @return Batch size
    */
    public int getBatchSize() {
    	return batchSize;
    }
   
   	/**
   	 *
   	 * @return Populating flag
   	 */
   	public boolean isPopulating() {
   		return populating;
   	}

    /**
    *
    * @param batchSize Batch size
    */
   	public void setPopulating(boolean populating) {
   		this.populating = populating;
   	}

    /**
     *
     * @param batchSize Batch size
     */
    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }
    
    //public void setCacheListener(MapListener listener) {
    //    this.listener = listener;
    //}

    /**
     *
     * @param store Cache store
     */
    public void setCacheStore(AbstractCacheStore store) {
        this.store = store;
    }

    /**
     *
     * @param indexes Indexes
     */
    public void setIndexes(List<ValueExtractor> indexes) {
		this.indexes = indexes;
	}
	
    /**
    *
    * @param triggers Triggers
    */
    public void setTriggers(List<MapTrigger> triggers) {
        this.triggers = triggers;
    }
   
    
	protected void ensureIndexes(NamedCache cache) {
	    if (indexes != null && indexes.size() > 0) {
        	for (ValueExtractor ex : indexes) {
        		cache.addIndex(ex, true, null);
    			log.debug("applied index: {}", ex);
        	}
	    }
	}

    protected void ensureTriggers(NamedCache cache) {
        if (triggers != null && triggers.size() > 0) {
            for (MapTrigger t : triggers) {
            	MapListener listener = getMapTriggerListener(t);
            	if (listener != null) {
            		cache.addMapListener(listener);
            		log.debug("applied trigger: {}", t);
            	}
            }
        }
    }
    
    protected MapListener getMapTriggerListener(MapTrigger t) {
    	return null;
    }

	//protected void ensureListener(NamedCache cache, BackingMapManagerContext ctx) {
	//    if (instantiateBML) {
	//        DelegatingBackingMapListener bml = new DelegatingBackingMapListener(ctx, cacheName);
	//        ctx.getBackingMapContext(cacheName).getBackingMap().addMapListener(bml);
	//    } else if (listener != null) {
	//		cache.addMapListener(listener);
	//	}
	//}

    /**
     * Run method
     */
    @Override
    public void run() {
        log.trace("run.enter");
        try {
            NamedCache cache = getCache();
            if (populating) {
            	CacheService svc = cache.getCacheService();
            	BackingMapManager mgr = svc.getBackingMapManager();
            	BackingMapManagerContext ctx = mgr.getContext();
            	if (store == null) {
            		Map map = ctx.getBackingMapContext(cache.getCacheName()).getBackingMap(); // got NPE here!!!
            		store = (AbstractCacheStore) ((ReadWriteBackingMap) map).getCacheStore().getStore(); 
            	}
            	populate(cache);
            }
            ensureIndexes(cache);
            ensureTriggers(cache);
            //ensureListener(cache, ctx);
        } catch (Exception ex) {
            log.error("Exception on population: {}", ex.getMessage(), ex);
        }
    }
    
    /**
     *
     * @param cache Named cache
     */
    public abstract void populate(NamedCache cache);

    /**
     * Invocation completed method
     */
    @Override
	public void invocationCompleted() {
	    log.debug("invocationCompleted");
	}

    /**
     *
     * @param member Member
     * @param result Object
     */
    @Override
	public void memberCompleted(Member member, Object result) {
	    log.debug("memberCompleted; result: {}, member: {}", result, member);
	}

    /**
     *
     * @param member Member
     * @param error in case of error
     */
    @Override
	public void memberFailed(Member member, Throwable error) {
        // TODO: resubmit tasks due to transient failures
    	log.error("memberFailed; member: {}", member, error);
	}

    /**
     *
     * @param member Member
     */
    @Override
	public void memberLeft(Member member) {
        // TODO: resubmit to a member that is up
    	log.error("memberLeft; member: {}", member);
	}

}
