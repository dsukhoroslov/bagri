package com.bagri.server.hazelcast.task.node;

import static com.bagri.core.Constants.pn_cluster_node_role;
import static com.bagri.server.hazelcast.serialize.TaskSerializationFactory.cli_factory_id;
import static com.bagri.server.hazelcast.serialize.TaskSerializationFactory.cli_KillNodeTask;
import static com.bagri.server.hazelcast.util.HazelcastUtils.hz_instance;

import java.io.IOException;
import java.util.Arrays;
import java.util.Set;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazelcast.client.HazelcastClient;
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
		HazelcastInstance hzSystem = null;
		Set<HazelcastInstance> instances = Hazelcast.getAllHazelcastInstances();
		logger.info("run; instances: {}", instances);
		int cnt = 0;
		for (HazelcastInstance instance: instances) {
			if (matchNames(instance.getName(), hz_instance)) {
				hzSystem = instance;
			} else if (shutdownInstance(instance)) {
				instance.shutdown();
				cnt++;
			}
		}
		logger.info("run.exit; instances stopped: {}", cnt);
		if (schemas.length == 0 || (hzSystem != null && instances.size() - cnt == 1)) {
			if ("admin".equals(hzSystem.getCluster().getLocalMember().getStringAttribute(pn_cluster_node_role))) {
				// close all open clients to cache nodes!
				for (HazelcastInstance client: HazelcastClient.getAllHazelcastClients()) {
					client.shutdown();
				}
			}
			hzSystem.shutdown();
			logger.info("run.exit; admin instance terminated as well");
			System.exit(0);
		}
	}
	
	private boolean shutdownInstance(HazelcastInstance hzInstance) {
		if (schemas.length == 0) {
			return true;
		}
		for (int i=0; i < schemas.length; i++) {
			if (matchNames(hzInstance.getName(), schemas[i])) {
				return true;
			}
		}
		return false;
	}
	
	private boolean matchNames(String instanceName, String schemaName) {
		return Pattern.compile(schemaName + "-\\d+").matcher(instanceName).matches();
	}

	@Override
	public int getFactoryId() {
		return cli_factory_id;
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
