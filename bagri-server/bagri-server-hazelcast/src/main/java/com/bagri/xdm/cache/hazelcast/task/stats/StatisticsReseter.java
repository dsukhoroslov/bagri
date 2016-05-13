package com.bagri.xdm.cache.hazelcast.task.stats;

import static com.bagri.xdm.client.hazelcast.serialize.DataSerializationFactoryImpl.cli_ResetStatisticsTask;

import java.util.concurrent.Callable;

import com.bagri.common.stats.StatisticsProvider;
import com.hazelcast.spring.context.SpringAware;

@SpringAware
public class StatisticsReseter extends StatisticsTask implements Callable<Boolean>  {

	public StatisticsReseter() {
		super();
	}

	public StatisticsReseter(String schemaName, String statsName) {
		super(schemaName, statsName);
	}

	@Override
	public int getId() {
		return cli_ResetStatisticsTask;
	}

	@Override
	public Boolean call() throws Exception {
		
		StatisticsProvider xdmStats = getStats();
		xdmStats.resetStatistics();
        return true; 
    }


}
