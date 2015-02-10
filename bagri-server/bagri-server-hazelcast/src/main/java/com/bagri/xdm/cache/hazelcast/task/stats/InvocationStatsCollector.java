package com.bagri.xdm.cache.hazelcast.task.stats;

import static com.bagri.xdm.client.hazelcast.serialize.XDMDataSerializationFactory.cli_CollectInvocationStatsTask;

import java.util.concurrent.Callable;

import javax.management.openmbean.TabularData;

import com.bagri.common.manage.InvocationStatistics;
import com.hazelcast.spring.context.SpringAware;

@SpringAware
public class InvocationStatsCollector extends InvocationStatsTask implements Callable<TabularData>  {

	
	public InvocationStatsCollector() {
		super();
	}

	public InvocationStatsCollector(String schemaName, String statsName) {
		super(schemaName, statsName);
	}

	@Override
	public int getId() {
		return cli_CollectInvocationStatsTask;
	}

	@Override
	public TabularData call() throws Exception {
		
		//if (xdmStats == null) {
			//ApplicationContext ctx = HazelcastUtils.findContext();
		//	ApplicationContext ctx = (ApplicationContext) SpringContextHolder.getContext(schemaName, "appContext");
		//	logger.debug("call; got stats: {}", xdmStats); 
		//	xdmProxy = ctx.getBean("xdmProxy", DocumentManagementProxy.class); 
		//}
		InvocationStatistics xdmStats = getStats();
        return xdmStats.getStatistics(); 
    }
	
}
