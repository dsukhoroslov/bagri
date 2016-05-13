package com.bagri.xdm.cache.hazelcast.task.stats;

import static com.bagri.xdm.client.hazelcast.serialize.DataSerializationFactoryImpl.cli_CollectStatisticSeriesTask;

import java.util.concurrent.Callable;

import javax.management.openmbean.TabularData;

import com.bagri.common.stats.StatisticsProvider;
import com.hazelcast.spring.context.SpringAware;

@SpringAware
public class StatisticSeriesCollector extends StatisticsTask implements Callable<TabularData>  {

	
	public StatisticSeriesCollector() {
		super();
	}

	public StatisticSeriesCollector(String schemaName, String statsName) {
		super(schemaName, statsName);
	}

	@Override
	public int getId() {
		return cli_CollectStatisticSeriesTask;
	}

	@Override
	public TabularData call() throws Exception {
		
		StatisticsProvider xdmStats = getStats();
        return xdmStats.getStatisticSeries(); 
    }
	
}
