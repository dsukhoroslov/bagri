package com.bagri.xdm.cache.hazelcast.task.module;

import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.xdm.cache.hazelcast.task.EntityProcessor;
import com.bagri.xdm.system.XDMModule;
import com.hazelcast.map.EntryBackupProcessor;
import com.hazelcast.map.EntryProcessor;

public abstract class ModuleProcessor extends EntityProcessor implements EntryProcessor<String, XDMModule>, 
	EntryBackupProcessor<String, XDMModule> {
		
	protected final transient Logger logger = LoggerFactory.getLogger(getClass());
	
	public ModuleProcessor() {
		//
	}
	
	public ModuleProcessor(int version, String admin) {
		super(version, admin);
	}
	
	@Override
	public void processBackup(Entry<String, XDMModule> entry) {
		process(entry);		
	}
	
	@Override
	public EntryBackupProcessor<String, XDMModule> getBackupProcessor() {
		return this;
	}

}
