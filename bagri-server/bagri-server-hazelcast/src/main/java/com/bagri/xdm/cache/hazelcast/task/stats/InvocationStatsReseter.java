package com.bagri.xdm.cache.hazelcast.task.stats;

import static com.bagri.xdm.client.hazelcast.serialize.XDMDataSerializationFactory.cli_InvocationStatsResetTask;

import java.util.concurrent.Callable;

import com.bagri.common.manage.InvocationStatistics;
import com.hazelcast.spring.context.SpringAware;

@SpringAware
public class InvocationStatsReseter extends InvocationStatsTask implements Callable<Boolean>  {

	public InvocationStatsReseter() {
		super();
	}

	public InvocationStatsReseter(String schemaName, String statsName) {
		super(schemaName, statsName);
	}

	@Override
	public int getId() {
		return cli_InvocationStatsResetTask;
	}

	@Override
	public Boolean call() throws Exception {
		
		//if (xdmStats == null) {
			//ApplicationContext ctx = HazelcastUtils.findContext();
		//	ApplicationContext ctx = (ApplicationContext) SpringContextHolder.getContext(schemaName, "appContext");
		//	logger.debug("call; got stats: {}", xdmStats); 
		//	xdmProxy = ctx.getBean("xdmProxy", DocumentManagementProxy.class); 
		//}
		InvocationStatistics xdmStats = getStats();
		xdmStats.resetStats();
        return true; 
    }


}
