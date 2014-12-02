package com.bagri.xdm.process.hazelcast;

import static com.bagri.xdm.access.hazelcast.pof.XDMDataSerializationFactory.cli_InvocationStatsCollectTask;

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
