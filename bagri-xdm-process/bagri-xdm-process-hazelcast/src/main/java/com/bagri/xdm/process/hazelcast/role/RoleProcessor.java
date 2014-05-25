package com.bagri.xdm.process.hazelcast.role;

import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.xdm.process.hazelcast.EntityProcessor;
import com.bagri.xdm.system.XDMRole;
import com.hazelcast.map.EntryBackupProcessor;
import com.hazelcast.map.EntryProcessor;

public abstract class RoleProcessor extends EntityProcessor implements EntryProcessor<String, XDMRole>, 
	EntryBackupProcessor<String, XDMRole> {
	
	protected final transient Logger logger = LoggerFactory.getLogger(getClass());

	public RoleProcessor() {
		//
	}

	public RoleProcessor(int version, String admin) {
		super(version, admin);
	}

	@Override
	public void processBackup(Entry<String, XDMRole> entry) {
		process(entry);		
	}

	@Override
	public EntryBackupProcessor<String, XDMRole> getBackupProcessor() {
		return this;
	}

}
