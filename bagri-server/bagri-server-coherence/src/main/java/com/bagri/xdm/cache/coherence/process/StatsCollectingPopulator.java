package com.bagri.xdm.cache.coherence.process;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.tangosol.net.NamedCache;

public abstract class StatsCollectingPopulator extends AbstractPopulator {

	protected int keys;
	protected int queried;
	protected int loaded;
	protected int batches;
	private Date startedAt;
	private Date finishedAt;
	
    public StatsCollectingPopulator(String cacheName) {
		super(cacheName);
	}

	protected void resetStats() {
		keys = 0;
		queried = 0;
		loaded = 0;
		batches = 0;
		finishedAt = null;
		startedAt = new Date();
	}
	
	protected void updateStats() {
		finishedAt = new Date();
	}
	
	public Map<String, Object> getStats() {
		Map<String, Object> stats = new HashMap<String, Object>(6);
		stats.put("Started At", startedAt == null ? "" : startedAt.toString());
		stats.put("Keys found", keys);
		stats.put("Records requested", queried);
		stats.put("Records loaded", loaded);
		stats.put("Batches processed", batches);
		stats.put("Finished At", finishedAt == null ? "" : finishedAt.toString());
		return stats;
	}

	
    /**
    *
    * @param cache Named cache
    */
	@Override
	public void populate(NamedCache cache) {
        long stamp = System.currentTimeMillis();
		resetStats();
		populateCache(cache);
		updateStats();
        stamp = System.currentTimeMillis() - stamp;
        log.info("Cache {} populated; time taken: {}", cache.getCacheName(), stamp);
	}
	
	protected abstract void populateCache(NamedCache cache);
	
}
