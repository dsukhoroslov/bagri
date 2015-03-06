package com.bagri.common.stats;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class UsageStatistics extends StatisticsCollector implements StatisticsProvider {

	public UsageStatistics(String name) {
		super(name);
	}
/*	
	@Override
	public TabularData getStatisticSeries() {
        TabularData result = null;
        for (Map.Entry<String, ResourceUsageStats> entry: rStats.entrySet()) {
            try {
                Map<String, Object> stats = entry.getValue().toMap();
                stats.put(sn_Resource, entry.getKey());
                CompositeData data = JMXUtils.mapToComposite(sn_Name, sn_Header, stats);
                result = JMXUtils.compositeToTabular(sn_Name, sn_Header, sn_Resource, result, data);
            } catch (Exception ex) {
                //logger.error("getStatisticSeries; error", ex);
            }
        }
        //logger.trace("getStatisticSeries.exit; returning: {}", result);
        return result;
    }
*/

	@Override
	protected Statistics createStatistics(String name) {
		return new ResourceUsageStatistics();
	}
	
	private class ResourceUsageStatistics implements Statistics {

		// stats names
		//public static final String sn_Avg_Time = "Avg time";
		private static final String sn_Miss = "Miss";
		private static final String sn_First = "First";
		private static final String sn_Accessed = "Accessed";
		private static final String sn_Last = "Last";
		//public static final String sn_Max_Time = "Max time";
		//public static final String sn_Min_Time = "Min time";
		private static final String sn_Hits = "Hits";
		//public static final String sn_Sum_Time = "Sum time";
		//public static final String sn_Throughput = "Throughput";
		
		private static final String sn_Name = "usage";
		private static final String sn_Header = "Resource Usage Statistics";
		private static final String sn_Resource = "Resource"; 
		
		private int cntAccessed;
		private int cntMiss;
		private int cntHits;
		
		//private long tmMin = Long.MAX_VALUE;
		//private long tmMax;
		//private long tmSum;
		private long tmFirst = 0;
		private long tmLast;
		
		@Override
		public String getDescription() {
			return sn_Resource;
		}

		@Override
		public String getHeader() {
			return sn_Header;
		}

		@Override
		public String getName() {
			return sn_Name;
		}

		@Override
		public void update(StatisticsEvent event) {
			cntAccessed += event.getCount();
			if (event.isSuccess()) {
				cntHits += event.getCount();
			} else {
				cntMiss += event.getCount();
			}
			tmLast = event.getTimestamp();
			if (tmFirst == 0) {
				tmFirst = tmLast;
			}
		}
		
		public Map<String, Object> toMap() {
			Map<String, Object> result = new HashMap<String, Object>(10);
			result.put(sn_First, new Date(tmFirst));
			result.put(sn_Last, new Date(tmLast));
			result.put(sn_Accessed, cntAccessed);
			result.put(sn_Miss, cntMiss);
			result.put(sn_Hits, cntHits);
			//result.put(sn_Max_Time, tmMax);
			//result.put(sn_Sum_Time, tmSum);
			return result;
		}
		
		@Override
		public String toString() {
			StringBuffer buff = new StringBuffer(sn_Header).append(" [");
			buff.append(sn_First).append(colon).append(new Date(tmFirst)).append(semicolon);
			buff.append(sn_Last).append(colon).append(new Date(tmLast)).append(semicolon);
			buff.append(sn_Accessed).append(colon).append(cntAccessed).append(semicolon);
			buff.append(sn_Miss).append(colon).append(cntMiss).append(semicolon);
			buff.append(sn_Hits).append(colon).append(cntHits).append(semicolon);
			buff.append("]");
			return buff.toString();
		}

	}

	
}
