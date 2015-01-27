package com.bagri.xdm.cache.hazelcast.task.doc;

import static com.bagri.xdm.client.hazelcast.serialize.XDMDataSerializationFactory.cli_InvocationStatsCollectTask;

import java.util.concurrent.Callable;

import javax.management.openmbean.TabularData;

import com.hazelcast.spring.context.SpringAware;

@SpringAware
public class DocumentStatsCollector extends DocumentStatsTask implements Callable<TabularData>  {

	@Override
	public int getId() {
		return cli_InvocationStatsCollectTask;
	}

	@Override
	public TabularData call() throws Exception {
		
		if (xdmStats == null) {
			//ApplicationContext ctx = HazelcastUtils.findContext();
		//	ApplicationContext ctx = (ApplicationContext) SpringContextHolder.getContext(schemaName, "appContext");
			logger.debug("call; got stats: {}", xdmStats); 
		//	xdmProxy = ctx.getBean("xdmProxy", DocumentManagementProxy.class); 
		}
		
        return xdmStats.getStatistics(); 
    }
	
}
