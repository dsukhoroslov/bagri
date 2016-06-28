package com.bagri.xdm.cache.hazelcast.task.library;

import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.xdm.cache.hazelcast.task.EntityProcessor;
import com.bagri.xdm.system.Library;
import com.hazelcast.map.EntryBackupProcessor;
import com.hazelcast.map.EntryProcessor;

public abstract class LibraryProcessor extends EntityProcessor implements EntryProcessor<String, Library>, 
			EntryBackupProcessor<String, Library> {
	
	protected final transient Logger logger = LoggerFactory.getLogger(getClass());
	
	public LibraryProcessor() {
		//
	}
	
	public LibraryProcessor(int version, String admin) {
		super(version, admin);
	}
	
	@Override
	public void processBackup(Entry<String, Library> entry) {
		process(entry);		
	}
	
	@Override
	public EntryBackupProcessor<String, Library> getBackupProcessor() {
		return this;
	}


}
