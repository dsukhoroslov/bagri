package com.bagri.server.hazelcast.task.module;

import static com.bagri.client.hazelcast.serialize.TaskSerializationFactory.cli_factory_id;
import static com.bagri.core.Constants.ctx_repo;
import static com.bagri.server.hazelcast.serialize.TaskSerializationFactory.cli_ReloadModuleTask;

import java.io.IOException;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.core.system.Module;
import com.bagri.server.hazelcast.impl.SchemaRepositoryImpl;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;

public class ModuleReloader implements Callable<Boolean>, IdentifiedDataSerializable {
	
	protected final transient Logger logger = LoggerFactory.getLogger(ModuleReloader.class);
	
	private Module module;
	
	public ModuleReloader() {
		// for de-ser ?
	}
	
	public ModuleReloader(Module module) {
		this.module = module;
	}

	@Override
	public Boolean call() throws Exception {
		logger.trace("call.enter; reloading module: {}", module);
		// refresh module in every found schemas..
		boolean result = false;
		for (HazelcastInstance hz: Hazelcast.getAllHazelcastInstances()) {
			SchemaRepositoryImpl repo = (SchemaRepositoryImpl) hz.getUserContext().get(ctx_repo);
			logger.debug("call; got repo: {} on instance: {}", repo, hz);
			if (repo != null) {
				repo.updateModule(module);
				result = true;
			}
		}
		logger.trace("call.exit; returning: {}", result);
		return result;
	}

	@Override
	public int getFactoryId() {
		return cli_factory_id;
	}

	@Override
	public int getId() {
		return cli_ReloadModuleTask;
	}

	@Override
	public void readData(ObjectDataInput in) throws IOException {
		module = in.readObject();
	}

	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		out.writeObject(module);
	}

}
