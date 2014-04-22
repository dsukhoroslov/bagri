package com.bagri.xdm.process.hazelcast;

import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.xdm.system.XDMSchema;
import com.hazelcast.map.EntryBackupProcessor;
import com.hazelcast.map.EntryProcessor;

public class SchemaProcessor implements EntryProcessor<String, XDMSchema>, EntryBackupProcessor<String, XDMSchema> {
	
	private static final transient Logger logger = LoggerFactory.getLogger(SchemaProcessor.class);

	@Override
	public void processBackup(Entry<String, XDMSchema> entry) {
		process(entry);		
	}

	@Override
	public EntryBackupProcessor<String, XDMSchema> getBackupProcessor() {
		return this;
	}

	@Override
	public Object process(Entry<String, XDMSchema> entry) {
		logger.debug("process.enter; entry: {}", entry); 
		return null;
	}

}
