package com.bagri.xdm.cache.hazelcast.impl;

import java.util.Map;

import com.bagri.common.stats.StatisticsEvent;
import com.bagri.common.stats.UsageStatistics;
import com.bagri.common.stats.StatisticsEvent.EventType;


public class QueryStatistics extends UsageStatistics {

	public QueryStatistics(String name) {
		super(name);
	}

	@Override
	protected Statistics createStatistics(String name) {
		return new QueryUsageStatistics();
	}
	
	private class QueryUsageStatistics extends ResourceUsageStatistics {
		
		private int results;
		private int hits;
		private int miss;

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
			if (event.getType() == EventType.index) {
				results += event.getCount();
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
	
}
