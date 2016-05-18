package com.bagri.xdm.cache.hazelcast.stats;

import java.util.Map;

import com.bagri.common.stats.StatisticsEvent;


public class QueryStatistics extends UsageStatistics {

	private int results;
	private int hits;
	private int miss;

	public QueryStatistics(String name) {
		super(name);
	}

	@Override
	public String getDescription() {
		return "Query Usage Statistics";
	}

	@Override
	public String getHeader() {
		return "Query";
	}

	@Override
	public String getName() {
		return "Query Statistics";
	}

	@Override
	public Map<String, Object> toMap() {
		Map<String, Object> result = super.toMap();
   		result.put("Cached Results", results);
   		result.put("Result Hits", hits);
   		result.put("Result Miss", miss);
		return result;
	}

	@Override
	public void update(StatisticsEvent event) {
		Object[]  params = event.getParams();
		if (params.length > 1) {
			results += (Integer) params[0];
			if (event.isSuccess()) {
				hits++;
			} else {
				miss++;
			}
		} else {
			super.update(event);
		}
	}
	
}
