package com.bagri.server.hazelcast.task.store;

import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.core.system.DataStore;
import com.bagri.server.hazelcast.task.EntityProcessor;
import com.hazelcast.map.EntryBackupProcessor;
import com.hazelcast.map.EntryProcessor;

public abstract class DataStoreProcessor extends EntityProcessor
		implements EntryProcessor<String, DataStore>, EntryBackupProcessor<String, DataStore> {

	protected final transient Logger logger = LoggerFactory.getLogger(getClass());

	public DataStoreProcessor() {
		//
	}

	public DataStoreProcessor(int version, String admin) {
		super(version, admin);
	}

	@Override
	public void processBackup(Entry<String, DataStore> entry) {
		process(entry);
	}

	@Override
	public EntryBackupProcessor<String, DataStore> getBackupProcessor() {
		return this;
	}

}
