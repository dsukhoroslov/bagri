package com.bagri.xdm.cache.hazelcast.task.user;

import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.xdm.cache.hazelcast.task.EntityProcessor;
import com.bagri.xdm.system.User;
import com.hazelcast.map.EntryBackupProcessor;
import com.hazelcast.map.EntryProcessor;

public abstract class UserProcessor extends EntityProcessor implements EntryProcessor<String, User>, 
	EntryBackupProcessor<String, User> {
	
	protected final transient Logger logger = LoggerFactory.getLogger(getClass());

	public UserProcessor() {
		//
	}
	
	public UserProcessor(int version, String admin) {
		super(version, admin);
	}
	
    @Override
	public void processBackup(Entry<String, User> entry) {
		process(entry);		
	}

	@Override
	public EntryBackupProcessor<String, User> getBackupProcessor() {
		return this;
	}

}
