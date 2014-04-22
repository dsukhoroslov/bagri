package com.bagri.xdm.cache.hazelcast.management;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.EntryListener;

public class SchemaEntryListener implements EntryListener {

    private static final transient Logger logger = LoggerFactory.getLogger(SchemaEntryListener.class);
    
    private SchemaManagement schemaManagement;
    
    public void setSchemaManagement(SchemaManagement sm) {
    	this.schemaManagement = sm;
    }
	
	@Override
	public void entryAdded(EntryEvent event) {
		logger.trace("entryAdded; event: {}", event); 
	}

	@Override
	public void entryEvicted(EntryEvent event) {
		logger.trace("entryEvicted; event: {}", event); 
	}

	@Override
	public void entryRemoved(EntryEvent event) {
		logger.trace("entryRemoved; event: {}", event); 
	}

	@Override
	public void entryUpdated(EntryEvent event) {
		logger.trace("entryUpdated; event: {}", event); 
	}

}
