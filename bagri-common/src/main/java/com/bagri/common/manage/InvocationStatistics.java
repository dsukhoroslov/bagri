package com.bagri.common.manage;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.TabularData;
import javax.management.openmbean.TabularDataSupport;
import javax.management.openmbean.TabularType;

public class InvocationStatistics {

	// this will be some cache, actually..
	private Map<String, MethodInvocationStats> mStats = new HashMap<String, MethodInvocationStats>();

	public Collection<String> getStatNames() {
		return mStats.keySet();
	}
	
	public Map<String, Object> getNamedStats(String name) {
		return mStats.get(name).toMap();
	}
	
	public TabularData getStatistics() {
        TabularData result = null;
        for (Map.Entry<String, MethodInvocationStats> entry: mStats.entrySet()) {
            try {
                Map<String, Object> stats = entry.getValue().toMap();
                stats.put("Method", entry.getKey());
                CompositeData data = JMXUtils.mapToComposite("invocation", "Method invocation statistics", stats);
                result = JMXUtils.compositeToTabular("invocation", "Method invocation statistics", "Method", 
                		result, data);
            } catch (Exception ex) {
                //logger.error("getStatisticSeries; error", ex);
            }
        }
        //logger.trace("getStatisticSeries.exit; returning: {}", result);
        return result;
    }

	public void initStats(String name) {
		MethodInvocationStats stats = new MethodInvocationStats();
		mStats.put(name, stats);
	}
	
	public void resetStats() {
		// renew all registered stats..
		for (String name: mStats.keySet()) {
			initStats(name);
		}
	}
	
	public void updateStats(String name, boolean success, long duration) {
		long now = System.currentTimeMillis();
		MethodInvocationStats miStats = mStats.get(name);
		if (miStats != null) {
			synchronized (miStats) {
				miStats.update(success, duration, now);
			}
		}
	}
	
	private class MethodInvocationStats {
		
		private int cntInvoked;
		private int cntFailed;
		private int cntSucceed;
		
		private long tmMin = Long.MAX_VALUE;
		private long tmMax;
		private long tmSum;
		private long tmFirst = 0;
		private long tmLast;
		
		void update(boolean success, long duration, long now) {
			cntInvoked++;
			tmSum += duration;
			if (duration < tmMin) {
				tmMin = duration;
			}
			if (duration > tmMax) {
				tmMax = duration;
			}
			if (success) {
				cntSucceed++;
			} else {
				cntFailed++;
			}
			tmLast = now;
			if (tmFirst == 0) {
				tmFirst = now;
			}
		}
		
		Map<String, Object> toMap() {
			Map<String, Object> result = new HashMap<String, Object>(10);
			result.put("First", new Date(tmFirst));
			result.put("Last", new Date(tmLast));
			result.put("Invoked", cntInvoked);
			result.put("Failed", cntFailed);
			result.put("Succeed", cntSucceed);
			result.put("Max time", tmMax);
			result.put("Sum time", tmSum);
			if (cntInvoked > 0) {
				result.put("Min time", tmMin);
				double dSum = tmSum;
				result.put("Avg time", dSum/cntInvoked);
				double dCnt = cntInvoked*1000;
				result.put("Throughput", dCnt/(tmLast - tmFirst));
			} else {
				result.put("Min time", 0L);
				result.put("Avg time", 0.0d);
				result.put("Throughput", 0.0d);
			}
			return result;
		}
		
		// how will we present stats ?
		@Override
		public String toString() {
			StringBuffer buff = new StringBuffer("Method Invocation Statistics [");
			buff.append("First: ").append(new Date(tmFirst)).append("; ");
			buff.append("Last: ").append(new Date(tmLast)).append("; ");
			buff.append("Invoked: ").append(cntInvoked).append("; ");
			buff.append("Failed: ").append(cntFailed).append("; ");
			buff.append("Succeed: ").append(cntSucceed).append("; ");
			buff.append("Max time: ").append(tmMax).append("; ");
			if (cntInvoked > 0) {
				buff.append("Min time: ").append(tmMin).append("; ");
				double dSum = tmSum;
				buff.append("Avg time: ").append(dSum/cntInvoked).append("; ");
				double dCnt = cntInvoked*1000;
				buff.append("Throughput: ").append(dCnt/(tmLast - tmFirst)).append("; ");
			} else {
				buff.append("Min time: 0; ");
				buff.append("Avg time: 0; ");
				buff.append("Throughput: 0; ");
			}
			buff.append("Sum time: ").append(tmSum);
			buff.append("]");
			return buff.toString();
		}
	}

}
