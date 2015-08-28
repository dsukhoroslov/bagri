package com.bagri.xdm.cache.hazelcast.task.stats;

import static com.bagri.xdm.client.hazelcast.serialize.XDMDataSerializationFactory.cli_CollectStatisticTotalsTask;

import java.util.concurrent.Callable;

import javax.management.openmbean.CompositeData;

import com.bagri.common.stats.StatisticsProvider;
import com.hazelcast.spring.context.SpringAware;

@SpringAware
public class StatisticTotalsCollector extends StatisticsTask implements Callable<CompositeData>  {

	
	public StatisticTotalsCollector() {
		super();
	}

	public StatisticTotalsCollector(String schemaName, String statsName) {
		super(schemaName, statsName);
	}

	@Override
	public int getId() {
		return cli_CollectStatisticTotalsTask;
	}

	@Override
	public CompositeData call() throws Exception {
		
		StatisticsProvider xdmStats = getStats();
        return xdmStats.getStatisticTotals(); 
    }
	
}


