package com.bagri.xdm.cache.hazelcast.task.schema;

import static com.bagri.xdm.client.hazelcast.serialize.XDMDataSerializationFactory.cli_AggregateSchemaHealthTask;
import static com.bagri.xdm.cache.hazelcast.util.SpringContextHolder.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import javax.management.openmbean.CompositeData;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import com.bagri.common.manage.JMXUtils;
import com.bagri.xdm.cache.hazelcast.impl.HealthManagementImpl;
import com.bagri.xdm.cache.hazelcast.impl.RepositoryImpl;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.Member;
import com.hazelcast.spring.context.SpringAware;

@SpringAware
public class SchemaHealthAggregator extends SchemaProcessingTask implements Callable<CompositeData> { 
	
	private static final transient Logger logger = LoggerFactory.getLogger(SchemaStatsAggregator.class);
    
	private HazelcastInstance hzInstance;
	private HealthManagementImpl hMgr;

	@Override
	public int getId() {
		return cli_AggregateSchemaHealthTask;
	}

    @Autowired
	public void setHealthManager(HealthManagementImpl hMgr) {
		this.hMgr = hMgr;
	}
    
    @Autowired
	public void setHazelcastInstance(HazelcastInstance hzInstance) {
		this.hzInstance = hzInstance;
	}

    @Override
	public CompositeData call() throws Exception {
		logger.info("call; HM: {}", hMgr);
		int[] counters = hMgr.getCounters();
		Map<String, Object> result = new HashMap<>(3);
		result.put("Active docs", counters[0]);
		result.put("Inactive docs", counters[1]);
		Member m = hzInstance.getCluster().getLocalMember();
		result.put("Member", m.getSocketAddress().toString() + " [" + m.getUuid() + "]"); 
		return JMXUtils.mapToComposite("Counters", "Description", result);
    }
    
}
