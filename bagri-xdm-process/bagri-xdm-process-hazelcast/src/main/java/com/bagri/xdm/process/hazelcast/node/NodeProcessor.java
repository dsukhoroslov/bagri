package com.bagri.xdm.process.hazelcast.node;

import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.xdm.process.hazelcast.EntityProcessor;
import com.bagri.xdm.system.XDMNode;
import com.hazelcast.map.EntryBackupProcessor;
import com.hazelcast.map.EntryProcessor;

public abstract class NodeProcessor extends EntityProcessor implements EntryProcessor<String, XDMNode>, 
	EntryBackupProcessor<String, XDMNode> {
	
	protected final transient Logger logger = LoggerFactory.getLogger(getClass());

	public NodeProcessor() {
		//
	}
	
	public NodeProcessor(int version, String admin) {
		super(version, admin);
	}
	
    @Override
	public void processBackup(Entry<String, XDMNode> entry) {
		process(entry);		
	}

	@Override
	public EntryBackupProcessor<String, XDMNode> getBackupProcessor() {
		return this;
	}


}
