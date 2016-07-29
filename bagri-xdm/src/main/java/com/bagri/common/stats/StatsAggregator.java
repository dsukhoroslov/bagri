package com.bagri.common.stats;

/**
 * Aggregates statistics series into one cumulative structure 
 * 
 * @author Denis Sukhoroslov
 *
 */
public interface StatsAggregator {
	
	/**
	 * 
	 * @param source the new statistics series (to add stats from)
	 * @param target the cumulative statistics structure (to add stats to)
	 * @return the aggregated statistics value (source + target)
	 */
	Object[] aggregateStats(Object[] source, Object[] target);

	// do we need an aggregate method for Map, CompositeData?
}
