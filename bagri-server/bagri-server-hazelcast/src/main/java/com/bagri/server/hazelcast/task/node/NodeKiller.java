package com.bagri.server.hazelcast.task.node;

import static com.bagri.core.Constants.pn_cluster_node_role;
import static com.bagri.client.hazelcast.serialize.DataSerializationFactoryImpl.factoryId;
import static com.bagri.server.hazelcast.serialize.DataSerializationFactoryImpl.cli_KillNodeTask;
import static com.bagri.server.hazelcast.util.HazelcastUtils.hz_instance;

import java.io.IOException;
import java.util.Arrays;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.core.Client;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;

public class NodeKiller implements Runnable, IdentifiedDataSerializable {
	
	private static final transient Logger logger = LoggerFactory.getLogger(NodeKiller.class);

	private String[] schemas;
	
	public NodeKiller() {
		schemas = new String[0];
	}
	
	public NodeKiller(String schemas) {
		this.schemas = schemas.split(", ");
	}
	
	@Override
	public void run() {
		logger.info("run.enter; about to stop schemas: {}", Arrays.toString(schemas));
		HazelcastInstance hzSystem = Hazelcast.getHazelcastInstanceByName(hz_instance);
		Set<HazelcastInstance> instances = Hazelcast.getAllHazelcastInstances();
		int cnt = 0;
		for (HazelcastInstance instance: instances) {
			if (!hz_instance.equals(instance.getName())) {
				if (shutdownInstance(instance)) {
					instance.shutdown();
					cnt++;
				}
			}
		}
		if (schemas.length == 0) {
			cnt++;
			if ("admin".equals(hzSystem.getCluster().getLocalMember().getStringAttribute(pn_cluster_node_role))) {
				// close all open clients to cache nodes!
				for (HazelcastInstance client: HazelcastClient.getAllHazelcastClients()) {
					client.shutdown();
				}
				hzSystem.shutdown();
				logger.info("run.exit; instances stopped: {}; admin terminated", cnt);
				System.exit(0);
			}
			hzSystem.shutdown();
		}
		logger.info("run.exit; instances stopped: {}", cnt);
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
