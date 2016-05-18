package com.bagri.xdm.cache.hazelcast.stats;

import static com.bagri.xdm.cache.hazelcast.stats.DocumentStatistics.all_docs;

import javax.management.openmbean.CompositeData;

import com.bagri.common.manage.JMXUtils;
import com.bagri.common.stats.Statistics;
import com.bagri.common.stats.StatisticsCollector;
import com.bagri.common.stats.StatisticsEvent;

public class DocumentStatsCollector extends StatisticsCollector<DocumentStatistics> {

	public DocumentStatsCollector(Class<DocumentStatistics> cls, String name) {
		super(cls, name);
		initStatistics(all_docs);
	}

	@Override
	public CompositeData getStatisticTotals() {
		DocumentStatistics result = (DocumentStatistics) series.get(all_docs);
        return JMXUtils.mapToComposite(name, "DocsStats", result.toMap());
	}

	@Override
	protected boolean reportStatistics(Statistics stats) {
		return !all_docs.equals(((DocumentStatistics) stats).getCollectionName());
	}
	
	@Override
	protected void updateStatistics(StatisticsEvent event) {
		if (event.getName() == null) {
			event = new StatisticsEvent(all_docs, event.isSuccess(), event.getParams());
		}
		super.updateStatistics(event);
	}
	
	
}
