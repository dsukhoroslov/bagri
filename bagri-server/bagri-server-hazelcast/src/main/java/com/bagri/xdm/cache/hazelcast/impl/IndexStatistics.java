package com.bagri.xdm.cache.hazelcast.impl;

import java.util.Map;

import com.bagri.common.stats.StatisticsEvent;
import com.bagri.common.stats.StatisticsEvent.EventType;
import com.bagri.common.stats.UsageStatistics;

public class IndexStatistics extends UsageStatistics {

	public IndexStatistics(String name) {
		super(name);
	}

	@Override
	protected Statistics createStatistics(String name) {
		return new IndexUsageStatistics();
	}
	
	private class IndexUsageStatistics extends ResourceUsageStatistics {
		
		private long size;
		private int count;
		private int unique;

		@Override
		public String getDescription() {
			return "Index Usage Statistics";
		}

		@Override
		public String getHeader() {
			return "Index";
		}

		@Override
		public String getName() {
			return "Index Statistics";
		}

		@Override
		public Map<String, Object> toMap() {
			Map<String, Object> result = super.toMap();
    		result.put("Indexed Documents", count);
    		result.put("Distinct Values", unique);
    		result.put("Consumed Size", size);
			return result;
		}

		@Override
		public void update(StatisticsEvent event) {
			if (event.getType() == EventType.index) {
				if (event.isSuccess()) {
					count += event.getCount();
					size += event.getSize();
					if (event.isUnique()) {
						unique++;
					}
				} else {
					count -= event.getCount();
					size -= event.getSize();
					if (event.isUnique()) {
						unique--;
					}
				}
			} else {
				super.update(event);
			}
		}
		
	}
	
}
