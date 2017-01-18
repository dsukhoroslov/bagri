package com.bagri.server.hazelcast.task.module;

import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.core.system.Module;
import com.bagri.server.hazelcast.task.EntityProcessor;
import com.hazelcast.map.EntryBackupProcessor;
import com.hazelcast.map.EntryProcessor;

public abstract class ModuleProcessor extends EntityProcessor implements EntryProcessor<String, Module>, 
	EntryBackupProcessor<String, Module> {
		
	protected final transient Logger logger = LoggerFactory.getLogger(getClass());
	
	public ModuleProcessor() {
		//
	}
	
	public ModuleProcessor(int version, String admin) {
		super(version, admin);
	}
	
	@Override
	public void processBackup(Entry<String, Module> entry) {
		process(entry);		
	}
	
	@Override
	public EntryBackupProcessor<String, Module> getBackupProcessor() {
		return this;
	}

}
