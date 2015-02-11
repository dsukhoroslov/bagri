package com.bagri.xdm.cache.hazelcast.management;

import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.openmbean.TabularData;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.jmx.export.naming.SelfNaming;

import com.bagri.common.manage.JMXUtils;
import com.bagri.xdm.cache.hazelcast.task.stats.StatisticSeriesCollector;
import com.bagri.xdm.cache.hazelcast.task.stats.StatisticsReseter;
import com.bagri.xdm.client.common.XDMCacheConstants;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.core.Member;
import com.hazelcast.core.MemberSelector;

@ManagedResource(description="Document Manager MBean")
public class DocumentManager implements SelfNaming {

    private static final transient Logger logger = LoggerFactory.getLogger(DocumentManager.class);
	
	private String uuid;
	private String schemaName;
	private HazelcastInstance hzInstance;
	
	public DocumentManager() {
		//
	}
	
	public DocumentManager(HazelcastInstance hzInstance, String schemaName, String uuid) {
		this.hzInstance = hzInstance;
		this.schemaName = schemaName;
		this.uuid = uuid;
	}

	@Override
	public ObjectName getObjectName() throws MalformedObjectNameException {
		return JMXUtils.getObjectName("type=Schema,name=" + schemaName + ",kind=DocumentManagement,node=" + uuid);
	}
	
	@ManagedAttribute(description="Returns DocumentManagement invocation statistics, per method")
	public TabularData getInvocationStatistics() {
		// must get stats from particular proxy. run task on that node..
		StatisticSeriesCollector task = new StatisticSeriesCollector(); 
		IExecutorService execService = hzInstance.getExecutorService(XDMCacheConstants.PN_XDM_SCHEMA_POOL);
		logger.trace("getInvocationStatistics.enter; going to collect stats from member: {}", uuid);

		Map<Member, Future<TabularData>> futures = execService.submitToMembers(task, new SchemaMemberSelector());
		//if (futures)
		try {
			Member member = futures.keySet().iterator().next();
			TabularData result = futures.get(member).get();
			logger.trace("getInvocationStatistics.exit; got result: {}", result);
			return result;
		} catch (InterruptedException | ExecutionException ex) {
			logger.error("getInvocationStatistics.error: " + ex.getMessage(), ex);
		}
		return null;
	}
    
	@ManagedOperation(description="Reset DocumentManagement invocation statistics")
	public void resetStatistics() {
		//
		StatisticsReseter task = new StatisticsReseter(); 
		IExecutorService execService = hzInstance.getExecutorService(XDMCacheConstants.PN_XDM_SCHEMA_POOL);
		logger.trace("resetStatistics.enter; going to reset stats on member: {}", uuid);

		Map<Member, Future<Boolean>> futures = execService.submitToMembers(task, new SchemaMemberSelector());
		//if (futures)
		try {
			Member member = futures.keySet().iterator().next();
			Boolean result = futures.get(member).get();
			logger.trace("resetStatistics.exit; got result: {}", result);
		} catch (InterruptedException | ExecutionException ex) {
			logger.error("resetStatistics.error: " + ex.getMessage(), ex);
		}
	}

	private class SchemaMemberSelector implements MemberSelector {

		@Override
		public boolean select(Member member) {
			boolean result = member.getUuid().equals(uuid);
			logger.trace("select; got Member: {}; UUID: {}; returning: {}", member, member.getUuid(), result);
			return result;
		}
		
	}
}
