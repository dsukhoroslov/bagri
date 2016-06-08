package com.bagri.common.stats;

public interface StatsAggregator {
	
	Object[] aggregateStats(Object[] source, Object[] target);

}
