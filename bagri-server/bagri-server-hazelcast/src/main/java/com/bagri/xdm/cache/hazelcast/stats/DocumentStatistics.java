package com.bagri.xdm.cache.hazelcast.stats;

import java.util.HashMap;
import java.util.Map;

import javax.management.openmbean.CompositeData;

import com.bagri.common.stats.Statistics;
import com.bagri.common.stats.StatisticsEvent;
import com.bagri.common.stats.StatisticsEvent.EventType;
import com.bagri.common.util.JMXUtils;

public class DocumentStatistics extends UsageStatistics {

	static final String all_docs = "All Documents";
	
	private String colName;
	private long size;
	private int count;
	private int elements;
	private int fragments;
		
	public DocumentStatistics(String name) {
		super(name);
		colName = name;
	}

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
		
	String getCollectionName() {
		return colName;
	}

	@Override
	public Map<String, Object> toMap() {
		Map<String, Object> result = new HashMap<String, Object>(4);
		result.put("Collection", colName);
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
		int elts = (Integer) event.getParam(0);
		int frags = (Integer) event.getParam(1);
		if (event.isSuccess()) {
			count++;
			elements += elts;
			fragments += frags;
			size += calcElementsSize(elts);
		} else {
			count--;
			elements -= elts;
			fragments -= frags;
			size -= calcElementsSize(elts);
		}
	}
		
	private long calcElementsSize(int count) {
		return count * ((8 + 8 + 4) // size of key
				+ (8 + 4 + 8) // size of value (XDMElements, not accurate!)
				+ (4 + 4 + 8)); // size of XDMElement
	}
	
}
