package com.bagri.xdm.cache.hazelcast.task.node;

import static com.bagri.xdm.client.hazelcast.serialize.DataSerializationFactoryImpl.cli_GetNodeInfoTask;
import static com.bagri.xdm.client.hazelcast.serialize.DataSerializationFactoryImpl.factoryId;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeData;

import com.bagri.common.manage.JMXUtils;
import com.bagri.common.util.DateUtils;
import com.hazelcast.core.Client;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;

public class NodeInfoProvider implements Callable<CompositeData>, IdentifiedDataSerializable { 
	
	public enum InfoType {
		memory,
		timing,
		client
	}
	
	private InfoType type;

	public NodeInfoProvider() {
	}

	public NodeInfoProvider(InfoType type) {
		this.type = type;
	}

	@Override
	public int getFactoryId() {
		return factoryId;
	}

	@Override
	public int getId() {
		return cli_GetNodeInfoTask;
	}

	@Override
	public CompositeData call() throws Exception {
		MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
		if (type == InfoType.memory) {
			ObjectName memName = new ObjectName("java.lang:type=Memory");
			return (CompositeData) mbs.getAttribute(memName, "HeapMemoryUsage");
		} else if (type == InfoType.timing) {
			ObjectName osName = new ObjectName("java.lang:type=Runtime");
			Map<String, Object> times = new HashMap<String, Object>(2);
			long time = (Long) mbs.getAttribute(osName, "StartTime");
	        times.put("StartTime",  new Date(time).toString());
	        time = (Long) mbs.getAttribute(osName, "Uptime");
	        times.put("Uptime", DateUtils.getDuration(time));
			return JMXUtils.mapToComposite("Timings", "Timing info", times);
		} else {
			Set<HazelcastInstance> instances = Hazelcast.getAllHazelcastInstances();
			Map<String, Object> clients = new HashMap<String, Object>();
			for (HazelcastInstance hzInstance: instances) {
				for (Client client: hzInstance.getClientService().getConnectedClients()) {
					clients.put(client.getUuid(), client.getSocketAddress().toString());
				}
			}
			return JMXUtils.mapToComposite("Clients", "Clients info", clients);
		}
	}

	@Override
	public void readData(ObjectDataInput in) throws IOException {
		type = InfoType.valueOf(in.readUTF());
	}

	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		out.writeUTF(type.name());
	}

}
