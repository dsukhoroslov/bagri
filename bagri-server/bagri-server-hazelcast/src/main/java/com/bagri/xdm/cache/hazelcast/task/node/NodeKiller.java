package com.bagri.xdm.cache.hazelcast.task.node;

import static com.bagri.xdm.cache.hazelcast.util.HazelcastUtils.hz_instance;
import static com.bagri.xdm.client.hazelcast.serialize.XDMDataSerializationFactory.cli_KillNodeTask;
import static com.bagri.xdm.client.hazelcast.serialize.XDMDataSerializationFactory.factoryId;

import java.io.IOException;
import java.util.Set;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;

public class NodeKiller implements Runnable, IdentifiedDataSerializable {
	
	private String[] schemas;
	
	public NodeKiller() {
		schemas = new String[0];
	}
	
	public NodeKiller(String schemas) {
		this.schemas = schemas.split(", ");
	}
	
	@Override
	public void run() {
		HazelcastInstance hzSystem = Hazelcast.getHazelcastInstanceByName(hz_instance);
		Set<HazelcastInstance> instances = Hazelcast.getAllHazelcastInstances();
		for (HazelcastInstance instance: instances) {
			if (!hz_instance.equals(instance.getName())) {
				if (shutdownInstance(instance)) {
					instance.shutdown();
				}
			}
		}
		if (schemas.length == 0) {
			hzSystem.shutdown();
			//System.exit(0);
		}
	}
	
	private boolean shutdownInstance(HazelcastInstance hzInstance) {
		if (schemas.length == 0) {
			return true;
		}
		for (int i=0; i < schemas.length; i++) {
			if (hzInstance.getName().equals(schemas[i])) {
				return true;
			}
		}
		return false;
	}

	@Override
	public int getFactoryId() {
		return factoryId;
	}

	@Override
	public int getId() {
		return cli_KillNodeTask;
	}

	@Override
	public void readData(ObjectDataInput in) throws IOException {
		int len = in.readInt();
		schemas = new String[len];
		for (int i=0; i < len; i++) {
			schemas[i] = in.readUTF();
		}
	}

	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		out.writeInt(schemas.length);
		for (int i=0; i < schemas.length; i++) {
			out.writeUTF(schemas[i]);
		}
	}

}
