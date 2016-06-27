package com.bagri.xdm.cache.hazelcast.task.schema;

import static com.bagri.xdm.cache.hazelcast.serialize.DataSerializationFactoryImpl.cli_AggregateSchemaHealthTask;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import javax.management.openmbean.CompositeData;

import org.springframework.beans.factory.annotation.Autowired;

import com.bagri.common.util.JMXUtils;
import com.bagri.xdm.cache.hazelcast.impl.HealthManagementImpl;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.Member;
import com.hazelcast.spring.context.SpringAware;

@SpringAware
public class SchemaHealthAggregator extends SchemaProcessingTask implements Callable<CompositeData> { 
	
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
		int[] counters = hMgr.getCounters();
		Map<String, Object> result = new HashMap<>(3);
		result.put("Active docs", counters[0]);
		result.put("Inactive docs", counters[1]);
		Member m = hzInstance.getCluster().getLocalMember();
		result.put("Member", m.getSocketAddress().toString() + " [" + m.getUuid() + "]"); 
		return JMXUtils.mapToComposite("Counters", "Description", result);
    }
    
}
