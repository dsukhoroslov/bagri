package com.bagri.xdm.cache.hazelcast.stats;

//import static com.bagri.common.stats.UsageStatistics.*;

import com.bagri.common.manage.StatsAggregator;

public class UsageStatsAggregator implements StatsAggregator {
	
	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Object[] aggregateStats(Object[] source, Object[] target) {
		
		target[0] = (Integer) source[0] + (Integer) target[0]; // accessed
		target[1] = ((Comparable) source[1]).compareTo((Comparable) target[1]) < 0 ? source[1] : target[1]; // first  
		target[2] = (Integer) source[2] + (Integer) target[2]; // hits
		target[3] = ((Comparable) source[3]).compareTo((Comparable) target[3]) > 0 ? source[3] : target[3]; // last  
		target[4] = (Integer) source[4] + (Integer) target[4]; // miss
		target[5] = source[5]; // resource
		return target;
	}

}
