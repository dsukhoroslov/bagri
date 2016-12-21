package com.bagri.server.hazelcast.stats;

import static com.bagri.server.hazelcast.stats.DocumentStatistics.all_docs;

import javax.management.openmbean.CompositeData;

import com.bagri.support.stats.Statistics;
import com.bagri.support.stats.StatisticsCollector;
import com.bagri.support.stats.StatisticsEvent;
import com.bagri.support.util.JMXUtils;

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
