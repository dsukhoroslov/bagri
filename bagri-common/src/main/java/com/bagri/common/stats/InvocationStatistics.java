package com.bagri.common.stats;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.TabularData;
import javax.management.openmbean.TabularDataSupport;
import javax.management.openmbean.TabularType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.common.manage.JMXUtils;

public class InvocationStatistics implements Runnable, StatisticsProvider {

	// stats indexes provided in alphabetical order
	public static final int idx_Avg_Time = 0;
	public static final int idx_Failed = 1;
	public static final int idx_First = 2;
	public static final int idx_Invoked = 3;
	public static final int idx_Last = 4;
	public static final int idx_Max_Time = 5;
	public static final int idx_Method = 6;
	public static final int idx_Min_Time = 7;
	public static final int idx_Succeed = 8;
	public static final int idx_Sum_Time = 9;
	public static final int idx_Throughput = 10;
	
	// stats names
	public static final String sn_Avg_Time = "Avg time";
	public static final String sn_Failed = "Failed";
	public static final String sn_First = "First";
	public static final String sn_Invoked = "Invoked";
	public static final String sn_Last = "Last";
	public static final String sn_Max_Time = "Max time";
	public static final String sn_Method = "Method"; 
	public static final String sn_Min_Time = "Min time";
	public static final String sn_Succeed = "Succeed";
	public static final String sn_Sum_Time = "Sum time";
	public static final String sn_Throughput = "Throughput";
	
	private static final String sn_Name = "invocation";
	private static final String sn_Header = "Method Invocation Statistics";
	private static final String colon = ": ";
	private static final String semicolon = "; ";
	private static final String empty = ": 0; ";
	
    private final Logger logger;
	
	private BlockingQueue<InvocationEvent> queue =  null;
	// this will be some cache, probably..?
	private Map<String, MethodInvocationStats> mStats = new HashMap<String, MethodInvocationStats>();
	
	public InvocationStatistics(String name) {
		logger = LoggerFactory.getLogger(getClass().getName() + "[" + name + "]");
	}
	
	public Collection<String> getStatsNames() {
		return mStats.keySet();
	}
	
	public Map<String, Object> getNamedStats(String methodName) {
		return mStats.get(methodName).toMap();
	}
	
	@Override
	public CompositeData getStatisticTotals() {
		// it just has no much sense to implement this method for invocation stats;
		return null;
	}
	
	@Override
	public TabularData getStatisticSeries() {
        TabularData result = null;
        for (Map.Entry<String, MethodInvocationStats> entry: mStats.entrySet()) {
            try {
                Map<String, Object> stats = entry.getValue().toMap();
                stats.put(sn_Method, entry.getKey());
                CompositeData data = JMXUtils.mapToComposite(sn_Name, sn_Header, stats);
                result = JMXUtils.compositeToTabular(sn_Name, sn_Header, sn_Method, result, data);
            } catch (Exception ex) {
                logger.error("getStatisticSeries; error", ex);
            }
        }
        //logger.trace("getStatisticSeries.exit; returning: {}", result);
        return result;
    }

	public MethodInvocationStats initStats(String name) {
		MethodInvocationStats miStats = new MethodInvocationStats();
		mStats.put(name, miStats);
		return miStats;
	}
	
	@Override
	public void resetStatistics() {
		// renew all registered stats..
		for (String name: mStats.keySet()) {
			initStats(name);
		}
	}

	@Override
	public void run() {
		logger.info("run; start"); 
		boolean running = true;
		while (running) {
			try {
				InvocationEvent event = queue.take();
				updateStats(event.getName(), event.isSuccess(), event.getDuration());
			} catch (InterruptedException ex) {
				logger.warn("run; interrupted");
				running = false;
			}
		}
		logger.info("run; finish"); 
	}

	public void setStatsQueue(BlockingQueue<InvocationEvent> queue) {
		if (this.queue == null) {
			this.queue = queue;
			Thread listener = new Thread(this);
			listener.start();
		} else {
			logger.warn("setStatsQueue; stats queue is already assigned: {}", queue);
		}
	}
	
	public void updateStats(String name, boolean success, long duration) {
		long now = System.currentTimeMillis();
		MethodInvocationStats miStats = mStats.get(name);
		if (miStats == null) {
			miStats = initStats(name);
		}
		miStats.update(success, duration, now);
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
			result.put(sn_First, new Date(tmFirst));
			result.put(sn_Last, new Date(tmLast));
			result.put(sn_Invoked, cntInvoked);
			result.put(sn_Failed, cntFailed);
			result.put(sn_Succeed, cntSucceed);
			result.put(sn_Max_Time, tmMax);
			result.put(sn_Sum_Time, tmSum);
			if (cntInvoked > 0) {
				result.put(sn_Min_Time, tmMin);
				//double dSum = tmSum;
				double tmAvg = tmSum/cntInvoked;
				result.put(sn_Avg_Time, tmAvg);
				double dCnt = cntInvoked*1000;
				result.put(sn_Throughput, dCnt/(tmLast - tmFirst + tmAvg));
			} else {
				result.put(sn_Min_Time, 0L);
				result.put(sn_Avg_Time, 0.0d);
				result.put(sn_Throughput, 0.0d);
			}
			return result;
		}
		
		// how will we present stats ?
		@Override
		public String toString() {
			StringBuffer buff = new StringBuffer(sn_Header).append(" [");
			buff.append(sn_First).append(colon).append(new Date(tmFirst)).append(semicolon);
			buff.append(sn_Last).append(colon).append(new Date(tmLast)).append(semicolon);
			buff.append(sn_Invoked).append(colon).append(cntInvoked).append(semicolon);
			buff.append(sn_Failed).append(colon).append(cntFailed).append(semicolon);
			buff.append(sn_Succeed).append(colon).append(cntSucceed).append(semicolon);
			buff.append(sn_Max_Time).append(colon).append(tmMax).append(semicolon);
			if (cntInvoked > 0) {
				buff.append(sn_Min_Time).append(colon).append(tmMin).append(semicolon);
				//double dSum = tmSum;
				double tmAvg = tmSum/cntInvoked;
				buff.append(sn_Avg_Time).append(colon).append(tmAvg).append(semicolon);
				double dCnt = cntInvoked*1000;
				buff.append(sn_Throughput).append(colon).append(dCnt/(tmLast - tmFirst + tmAvg)).append(semicolon);
			} else {
				buff.append(sn_Min_Time).append(empty);
				buff.append(sn_Avg_Time).append(empty);
				buff.append(sn_Throughput).append(empty);
			}
			buff.append(sn_Sum_Time).append(colon).append(tmSum);
			buff.append("]");
			return buff.toString();
		}
	}

}
