package com.bagri.xdm.cache.hazelcast.impl;

import java.util.Map;

import com.bagri.common.stats.StatisticsEvent;
import com.bagri.common.stats.UsageStatistics;
import com.bagri.common.stats.StatisticsEvent.EventType;

public class DocumentStatistics extends UsageStatistics {

	public DocumentStatistics(String name) {
		super(name);
	}

	@Override
	protected Statistics createStatistics(String name) {
		return new DocUsageStatistics();
	}
	
	private class DocUsageStatistics extends ResourceUsageStatistics {
		
		private int count;
		private int elements;
		private int fragments;

		@Override
		public String getDescription() {
			return "Collection Statistics";
		}

		@Override
		public String getHeader() {
			return "Collection";
		}

		@Override
		public String getName() {
			return "Collection Statistics";
		}

		@Override
		public Map<String, Object> toMap() {
			Map<String, Object> result = super.toMap();
    		result.put("Number of documents", count);
    		result.put("Number of elements", elements);
    		result.put("Number of fragments", fragments);
			return result;
		}

		@Override
		public void update(StatisticsEvent event) {
			if (event.getType() == EventType.use) {
				if (event.isSuccess()) {
					count++;
					elements += event.getSize();
					fragments += event.getCount();
				} else {
					count--;
					elements -= event.getSize();
					fragments -= event.getCount();
				}
			} else {
				super.update(event);
			}
		}
		
	}
	
}
