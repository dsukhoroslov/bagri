package com.bagri.xdm.cache.hazelcast.task.format;

import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.xdm.cache.hazelcast.task.EntityProcessor;
import com.bagri.xdm.system.XDMDataFormat;
import com.hazelcast.map.EntryBackupProcessor;
import com.hazelcast.map.EntryProcessor;

public abstract class DataFormatProcessor extends EntityProcessor implements EntryProcessor<String, XDMDataFormat>, 
	EntryBackupProcessor<String, XDMDataFormat> {
		
	protected final transient Logger logger = LoggerFactory.getLogger(getClass());
	
	public DataFormatProcessor() {
		//
	}
	
	public DataFormatProcessor(int version, String admin) {
		super(version, admin);
	}
	
	@Override
	public void processBackup(Entry<String, XDMDataFormat> entry) {
		process(entry);		
	}
	
	@Override
	public EntryBackupProcessor<String, XDMDataFormat> getBackupProcessor() {
		return this;
	}

}
