package com.bagri.xdm.cache.hazelcast.stats;

import java.util.Map;

import com.bagri.common.stats.StatisticsEvent;

public class IndexStatistics extends UsageStatistics {

	private long size;
	private int count;
	private int unique;

	public IndexStatistics(String name) {
		super(name);
	}

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
		Object[] params = event.getParams();
		if (params.length > 1) {
			int cnt = (Integer) params[0];
			int sz = (Integer) params[1];
			boolean un = (Boolean) params[2];
			if (event.isSuccess()) {
				count += cnt;
				size += sz;
				if (un) {
					unique++;
				}
			} else {
				count -= cnt;
				size -= sz;
				if (un) {
					unique--;
				}
			}
		} else {
			super.update(event);
		}
	}
	
}
