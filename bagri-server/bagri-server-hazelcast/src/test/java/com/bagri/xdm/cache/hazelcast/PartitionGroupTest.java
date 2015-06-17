package com.bagri.xdm.cache.hazelcast;

import java.util.Set;

import junit.framework.Assert;

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

public class PartitionGroupTest {

	@Test
	public void testPartitionGroups() throws Exception {
	
		String adminIP = "127.0.0.1";
		String localIP = "10.249.143.189";
		
		Config adminConfig = new Config();
		adminConfig.getNetworkConfig().setPort(5781);
		adminConfig.getNetworkConfig().setPortAutoIncrement(true);
		NetworkConfig network = adminConfig.getNetworkConfig();
		JoinConfig join = network.getJoin();
		join.getMulticastConfig().setEnabled(false);
		join.getTcpIpConfig().addMember(adminIP).addMember(localIP).setEnabled(true);
		PartitionGroupConfig partitionGroupConfig = adminConfig.getPartitionGroupConfig();
		partitionGroupConfig.setEnabled(true).setGroupType(MemberGroupType.CUSTOM);
		MemberGroupConfig adminGroupConfig = new MemberGroupConfig();
		adminGroupConfig.addInterface(adminIP);
		MemberGroupConfig cacheGroupConfig = new MemberGroupConfig();
		cacheGroupConfig.addInterface(localIP);
		partitionGroupConfig.addMemberGroupConfig(adminGroupConfig);
		partitionGroupConfig.addMemberGroupConfig(cacheGroupConfig);
		HazelcastInstance admin = Hazelcast.newHazelcastInstance(adminConfig);
		
		IMap<Long, String> adminMap = admin.getMap("test");
		for (long i=0; i < 100; i++) {
			adminMap.put(i, String.valueOf(i));
		}

		Config cacheConfig = new Config();
		cacheConfig.getNetworkConfig().setPort(5781);
		cacheConfig.getNetworkConfig().setPortAutoIncrement(true);
		network = cacheConfig.getNetworkConfig();
		join = network.getJoin();
		join.getMulticastConfig().setEnabled(false);
		join.getTcpIpConfig().addMember(adminIP).addMember(localIP).setEnabled(true);
		partitionGroupConfig = cacheConfig.getPartitionGroupConfig();
		partitionGroupConfig.setEnabled(true).setGroupType(MemberGroupType.CUSTOM);
		//MemberGroupConfig adminGroupConfig = new MemberGroupConfig();
		//adminGroupConfig.addInterface(adminIP);
		//MemberGroupConfig cacheGroupConfig = new MemberGroupConfig();
		//cacheGroupConfig.addInterface(localIP);
		partitionGroupConfig.addMemberGroupConfig(adminGroupConfig);
		partitionGroupConfig.addMemberGroupConfig(cacheGroupConfig);
		HazelcastInstance cache = Hazelcast.newHazelcastInstance(cacheConfig);

		Thread.sleep(5000);
		
		Set<Long> keys = adminMap.localKeySet();
		System.out.println("admin keys size is: " + keys.size());
		IMap<Long, String> cacheMap = cache.getMap("test");
		keys = cacheMap.localKeySet();
		System.out.println("cache keys size is: " + keys.size());
		Assert.assertTrue(keys.size() == 0);
	}	
	
	
}
