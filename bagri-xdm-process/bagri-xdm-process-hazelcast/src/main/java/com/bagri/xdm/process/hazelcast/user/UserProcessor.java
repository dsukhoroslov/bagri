package com.bagri.xdm.process.hazelcast.user;

import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.xdm.process.hazelcast.EntityProcessor;
import com.bagri.xdm.system.XDMUser;
import com.hazelcast.map.EntryBackupProcessor;
import com.hazelcast.map.EntryProcessor;

public abstract class UserProcessor extends EntityProcessor implements EntryProcessor<String, XDMUser>, 
	EntryBackupProcessor<String, XDMUser> {
	
	protected final transient Logger logger = LoggerFactory.getLogger(getClass());

	public UserProcessor() {
		//
	}
	
	public UserProcessor(int version, String admin) {
		super(version, admin);
	}
	
    @Override
	public void processBackup(Entry<String, XDMUser> entry) {
		process(entry);		
	}

	@Override
	public EntryBackupProcessor<String, XDMUser> getBackupProcessor() {
		return this;
	}

}
