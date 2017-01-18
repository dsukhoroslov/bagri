package com.bagri.server.hazelcast.stats;

import static com.bagri.server.hazelcast.stats.InvocationStatistics.*;

import java.util.Date;

import com.bagri.support.stats.StatsAggregator;

public class InvocationStatsAggregator implements StatsAggregator {
	
	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Object[] aggregateStats(Object[] source, Object[] target) {

		target[idx_Failed] = (Integer) source[idx_Failed] + (Integer) target[idx_Failed];
		target[idx_First] = ((Comparable) source[idx_First]).compareTo((Comparable) target[idx_First]) < 0 ? source[idx_First] : target[idx_First];  
		target[idx_Invoked] = (Integer) source[idx_Invoked] + (Integer) target[idx_Invoked];
		target[idx_Last] = ((Comparable) source[idx_Last]).compareTo((Comparable) target[idx_Last]) > 0 ? source[idx_Last] : target[idx_Last];  
		target[idx_Max_Time] = ((Comparable) source[idx_Max_Time]).compareTo((Comparable) target[idx_Max_Time]) > 0 ? source[idx_Max_Time] : target[idx_Max_Time];
		target[idx_Method] = source[idx_Method];
		target[idx_Min_Time] = ((Comparable) source[idx_Min_Time]).compareTo((Comparable) target[idx_Min_Time]) < 0 ? source[idx_Min_Time] : target[idx_Min_Time];
		target[idx_Succeed] = (Integer) source[idx_Succeed] + (Integer) target[idx_Succeed];
		target[idx_Sum_Time] = (Long) source[idx_Sum_Time] + (Long) target[idx_Sum_Time];
		
		int cntInvoke = (Integer) target[idx_Invoked];
		long tmFirst = ((Date) target[idx_First]).getTime();
		long tmLast = ((Date) target[idx_Last]).getTime();
		long tmMin = (Long) target[idx_Min_Time];
		long tmSum = (Long) target[idx_Sum_Time];
		if (cntInvoke > 0) {
	        double dSum = tmSum;
	        target[idx_Avg_Time] = dSum/cntInvoke;
			double dCnt = 1000.0d;
			long tmDuration = tmLast - tmFirst + tmMin; //tmAvg;
			target[idx_Throughput] = dCnt*cntInvoke/tmDuration;
			target[idx_Duration] = tmDuration;
		} else {
	        target[idx_Avg_Time] = 0.0d;
			target[idx_Throughput] = 0.0d;
			target[idx_Duration] = 0L;
		}
		
		return target;
	}

}
