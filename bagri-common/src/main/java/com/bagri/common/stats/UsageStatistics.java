package com.bagri.common.stats;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.TabularData;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.common.manage.JMXUtils;
import com.bagri.common.stats.StatisticsProvider;

public class UsageStatistics implements Runnable, StatisticsProvider {

	// stats names
	//public static final String sn_Avg_Time = "Avg time";
	public static final String sn_Miss = "Miss";
	public static final String sn_First = "First";
	public static final String sn_Accessed = "Accessed";
	public static final String sn_Last = "Last";
	//public static final String sn_Max_Time = "Max time";
	public static final String sn_Resource = "Resource"; 
	//public static final String sn_Min_Time = "Min time";
	public static final String sn_Hits = "Hits";
	//public static final String sn_Sum_Time = "Sum time";
	//public static final String sn_Throughput = "Throughput";

	private static final String sn_Name = "usage";
	private static final String sn_Header = "Resource Usage Statistics";
	private static final String colon = ": ";
	private static final String semicolon = "; ";
	
    private final Logger logger;
	
	private BlockingQueue<UsageEvent> queue =  null;
	private Map<String, ResourceUsageStats> rStats = new HashMap<String, ResourceUsageStats>();

	public UsageStatistics(String name) {
		logger = LoggerFactory.getLogger(getClass().getName() + "[" + name + "]");
	}
	
	public Collection<String> getStatsNames() {
		return rStats.keySet();
	}
	
	public Map<String, Object> getNamedStats(String resourceName) {
		return rStats.get(resourceName).toMap();
	}
	
	@Override
	public CompositeData getStatisticTotals() {
		// it just has no much sense to implement this method for usage stats;
		return null;
	}
	
	@Override
	public TabularData getStatisticSeries() {
        TabularData result = null;
        for (Map.Entry<String, ResourceUsageStats> entry: rStats.entrySet()) {
            try {
                Map<String, Object> stats = entry.getValue().toMap();
                stats.put(sn_Resource, entry.getKey());
                CompositeData data = JMXUtils.mapToComposite(sn_Name, sn_Header, stats);
                result = JMXUtils.compositeToTabular(sn_Name, sn_Header, sn_Resource, result, data);
            } catch (Exception ex) {
                //logger.error("getStatisticSeries; error", ex);
            }
        }
        //logger.trace("getStatisticSeries.exit; returning: {}", result);
        return result;
    }

	public ResourceUsageStats initStats(String name) {
		ResourceUsageStats ruStats = new ResourceUsageStats();
		rStats.put(name, ruStats);
		return ruStats;
	}
	
	public void deleteStats(String name) {
		rStats.remove(name);
	}
	
	@Override
	public void resetStatistics() {
		// renew all registered stats..
		for (String name: rStats.keySet()) {
			initStats(name);
		}
	}
	
	@Override
	public void run() {
		logger.info("run; start"); 
		boolean running = true;
		while (running) {
			try {
				UsageEvent event = queue.take();
				updateStats(event.getName(), event.isSuccess(), event.getCount());
			} catch (InterruptedException ex) {
				logger.warn("run; interrupted");
				running = false;
			}
		}
		logger.info("run; finish"); 
	}

	public void setStatsQueue(BlockingQueue<UsageEvent> queue) {
		if (this.queue == null) {
			this.queue = queue;
			Thread listener = new Thread(this);
			listener.start();
		} else {
			logger.warn("setStatsQueue; stats queue is already assigned: {}", queue);
		}
	}
	
	public void updateStats(String name, boolean success, int count) {
		if (count > 0) {
			long now = System.currentTimeMillis();
			ResourceUsageStats ruStats = rStats.get(name);
			if (ruStats == null) {
				ruStats = initStats(name);
			}
			ruStats.update(success, now, count);
		}
	}

	private class ResourceUsageStats {
		
		private int cntAccessed;
		private int cntMiss;
		private int cntHits;
		
		//private long tmMin = Long.MAX_VALUE;
		//private long tmMax;
		//private long tmSum;
		private long tmFirst = 0;
		private long tmLast;
		
		void update(boolean success, long now, int count) {
			cntAccessed += count;
			if (success) {
				cntHits += count;
			} else {
				cntMiss += count;
			}
			tmLast = now;
			if (tmFirst == 0) {
				tmFirst = now;
			}
		}
		
		Map<String, Object> toMap() {
			Map<String, Object> result = new HashMap<String, Object>(10);
			result.put(sn_First, new Date(tmFirst));
			result.put(sn_Last, new Date(tmLast));
			result.put(sn_Accessed, cntAccessed);
			result.put(sn_Miss, cntMiss);
			result.put(sn_Hits, cntHits);
			//result.put(sn_Max_Time, tmMax);
			//result.put(sn_Sum_Time, tmSum);
			return result;
		}
		
		// how will we present stats ?
		@Override
		public String toString() {
			StringBuffer buff = new StringBuffer(sn_Header).append(" [");
			buff.append(sn_First).append(colon).append(new Date(tmFirst)).append(semicolon);
			buff.append(sn_Last).append(colon).append(new Date(tmLast)).append(semicolon);
			buff.append(sn_Accessed).append(colon).append(cntAccessed).append(semicolon);
			buff.append(sn_Miss).append(colon).append(cntMiss).append(semicolon);
			buff.append(sn_Hits).append(colon).append(cntHits).append(semicolon);
			buff.append("]");
			return buff.toString();
		}
	}
	
}
