package com.bagri.xdm.cache.hazelcast.impl;

import java.util.HashMap;
import java.util.Map;

import javax.management.openmbean.CompositeData;

import com.bagri.common.manage.JMXUtils;
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

	@Override
	public CompositeData getStatisticTotals() {
		DocUsageStatistics result = new DocUsageStatistics();
        for (Map.Entry<String, Statistics> entry: stats.entrySet()) {
            DocUsageStatistics sts = (DocUsageStatistics) entry.getValue();
            result.size += sts.size;
            result.count += sts.count;
            result.elements += sts.elements;
            result.fragments += sts.fragments;
        }
        return JMXUtils.mapToComposite(name, "DocsStats", result.toMap());
	}
	
	
	private class DocUsageStatistics implements Statistics {
		
		private long size;
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
			Map<String, Object> result = new HashMap<String, Object>(4);
    		result.put("Consumed size", size);
    		result.put("Number of documents", count);
    		result.put("Number of elements", elements);
    		result.put("Number of fragments", fragments);
			double dSize = size;
			double sAvg = dSize/count;
    		result.put("Avg size (bytes)", sAvg);
			dSize = elements;
			sAvg = dSize/count;
    		result.put("Avg size (elmts)", sAvg);
			return result;
		}

		@Override
		public void update(StatisticsEvent event) {
			if (event.getType() == EventType.use) {
				if (event.isSuccess()) {
					count++;
					elements += event.getSize();
					fragments += event.getCount();
					size += calcElementsSize((int) event.getSize());
				} else {
					count--;
					elements -= event.getSize();
					fragments -= event.getCount();
					size -= calcElementsSize((int) event.getSize());
				}
			//} else {
			//	super.update(event);
			}
		}
		
		private long calcElementsSize(int count) {
			return count * ((8 + 8 + 4) // size of key
					+ (8 + 4 + 8) // size of value (XDMElements, not accurate!)
					+ (4 + 4 + 8)); // size of XDMElement
		}
		
	}
	
}
