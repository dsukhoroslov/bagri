package com.bagri.xdm.cache.hazelcast;

import java.util.Collections;
import java.util.Set;

import static org.junit.Assert.*;

import org.junit.Ignore;
import org.junit.Test;

import com.hazelcast.config.Config;
import com.hazelcast.config.JoinConfig;
import com.hazelcast.config.MemberGroupConfig;
import com.hazelcast.config.NetworkConfig;
import com.hazelcast.config.PartitionGroupConfig;
import com.hazelcast.config.PartitionGroupConfig.MemberGroupType;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

public class MigrationTest {

	private boolean loadData = false;
	
	@Test
	@Ignore
	public void testDataMigration() throws Exception {
	
		String adminIP = "127.0.0.1";
		String cacheIP = "192.168.1.87";
		
		Config hzConfig = new Config();
		hzConfig.getNetworkConfig().setPortAutoIncrement(true);
		hzConfig.getGroupConfig().setName("system");
		hzConfig.getGroupConfig().setPassword("syspwd");
		NetworkConfig network = hzConfig.getNetworkConfig();
		JoinConfig join = network.getJoin();
		join.getMulticastConfig().setEnabled(false);
		join.getTcpIpConfig().addMember(adminIP).setEnabled(true);
		hzConfig.setProperty("hazelcast.logging.type", "slf4j");
		hzConfig.setLiteMember(true);
		HazelcastInstance node1 = Hazelcast.newHazelcastInstance(hzConfig);

		Thread.sleep(5000);

		Config shConfig = new Config();
		shConfig.getNetworkConfig().setPortAutoIncrement(true);
		shConfig.getGroupConfig().setName("schema");
		shConfig.getGroupConfig().setPassword("schemapass");
		JoinConfig shJoin = shConfig.getNetworkConfig().getJoin();
		shJoin.getMulticastConfig().setEnabled(false);
		shJoin.getTcpIpConfig().addMember(cacheIP).setEnabled(true);
		shConfig.setProperty("hazelcast.logging.type", "slf4j");
		shConfig.setLiteMember(false);
		HazelcastInstance node2 = Hazelcast.newHazelcastInstance(shConfig);
		
		IMap<Long, String> testMap = node2.getMap("test");
		int idx = testMap.size();
		for (long i=idx; i < idx+1000; i++) {
			testMap.put(i, String.valueOf(i));
		}

		Thread.sleep(50000);
		
		Set<Long> keys = testMap.localKeySet();
		System.out.println("node2 keys size is: " + keys.size());
		assertTrue(keys.size() > 0);
	}	
	
	
}
