package com.bagri.server.hazelcast;

import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import static org.junit.Assert.*;

import org.junit.Ignore;
import org.junit.Test;

import com.hazelcast.config.Config;
import com.hazelcast.config.JoinConfig;
import com.hazelcast.config.MapConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.MapLoaderLifecycleSupport;
import com.hazelcast.core.MapStore;

public class MigrationTest {

	@Test
	@Ignore
	public void testDataMigration() throws Exception {
	
		Config shConfig = new Config();
		shConfig.getNetworkConfig().setPortAutoIncrement(true);
		shConfig.getGroupConfig().setName("schema");
		shConfig.getGroupConfig().setPassword("schemapass");
		JoinConfig shJoin = shConfig.getNetworkConfig().getJoin();
		shJoin.getMulticastConfig().setEnabled(false);
		shJoin.getTcpIpConfig().addMember("localhost").setEnabled(true);
		shConfig.setProperty("hazelcast.logging.type", "slf4j");
		
		MapConfig mConfig = new MapConfig("test");
		mConfig.getMapStoreConfig().setEnabled(true);
		mConfig.getMapStoreConfig().setWriteDelaySeconds(10);
		mConfig.getMapStoreConfig().setImplementation(new TestCacheStore());
		shConfig.addMapConfig(mConfig);
		
		HazelcastInstance node1 = Hazelcast.newHazelcastInstance(shConfig);
		populateCache(node1, "test", 1000);

		System.out.println("population finished, time to start second node!");
		Thread.sleep(50000);
		
		//HazelcastInstance node2 = Hazelcast.newHazelcastInstance(shConfig);
		//populateCache(node, "test", 1000);

		//System.out.println("Second node started");
		//Thread.sleep(5000);
	}
	
	private void populateCache(HazelcastInstance node, String cName, int count) {

		IMap<Long, String> cache = node.getMap(cName);
		int idx = cache.size();
		for (long i=idx; i < idx+count; i++) {
			cache.set(i, String.valueOf(i));
		}
	}
	
	
	public class TestCacheStore implements MapStore<Integer, String>, MapLoaderLifecycleSupport {
		
		private IMap testCache;
		private HazelcastInstance hzInstance;

		@Override
		public String load(Integer key) {
			System.out.println("received load request for key " + key);
			return null;
		}

		@Override
		public Map<Integer, String> loadAll(Collection<Integer> keys) {
			System.out.println("received loadAll request for keys " + keys);
			return null;
		}

		@Override
		public Iterable<Integer> loadAllKeys() {
			System.out.println("received loadAllKeys request");
			//testCache = hzInstance.getMap("test2");
			return null; //Collections.emptySet();
		}

		@Override
		public void init(HazelcastInstance hzInstance, Properties properties, String mapName) {
			System.out.println("received init request for map " + mapName);
			testCache = hzInstance.getMap("test2");
			this.hzInstance = hzInstance;
		}

		@Override
		public void destroy() {
			// TODO Auto-generated method stub
		}

		@Override
		public void store(Integer key, String value) {
			// TODO Auto-generated method stub
		}

		@Override
		public void storeAll(Map<Integer, String> map) {
			// TODO Auto-generated method stub
		}

		@Override
		public void delete(Integer key) {
			// TODO Auto-generated method stub
		}

		@Override
		public void deleteAll(Collection<Integer> keys) {
			// TODO Auto-generated method stub
		}
		
	}
	
}
