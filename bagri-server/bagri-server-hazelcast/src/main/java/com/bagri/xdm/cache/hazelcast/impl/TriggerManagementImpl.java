package com.bagri.xdm.cache.hazelcast.impl;

import java.util.Map;
import java.util.concurrent.BlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.common.stats.StatisticsEvent;
import com.bagri.xdm.cache.api.XDMTriggerManagement;
import com.bagri.xdm.domain.XDMDocumentType;
import com.bagri.xdm.system.XDMTriggerDef;
import com.bagri.xdm.system.XDMTriggerDef.Scope;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.core.IMap;

public class TriggerManagementImpl implements XDMTriggerManagement {

	private static final transient Logger logger = LoggerFactory.getLogger(TriggerManagementImpl.class);
	private IMap<Integer, XDMTriggerDef> trgDict;
    //private IMap<XDMIndexKey, XDMIndexedValue> idxCache;
	//private IExecutorService execService;

	//private XDMFactory factory;
    //private ModelManagementImpl mdlMgr;
    
    private boolean enableStats = true;
	private BlockingQueue<StatisticsEvent> queue;
	
	protected Map<Integer, XDMTriggerDef> getTriggerDictionary() {
		return trgDict;
	}
	
	public void setTriggerDictionary(IMap<Integer, XDMTriggerDef> trgDict) {
		this.trgDict = trgDict;
	}
	
    public void setStatsQueue(BlockingQueue<StatisticsEvent> queue) {
    	this.queue = queue;
    }

    public void setStatsEnabled(boolean enable) {
    	this.enableStats = enable;
    }

	@Override
	public boolean isTriggerRigistered(int typeId, Scope scope) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void createTrigger(XDMTriggerDef trigger, XDMDocumentType docType) {
		// TODO Auto-generated method stub
	}

	@Override
	public void deleteTrigger(XDMTriggerDef trigger, XDMDocumentType docType) {
		// TODO Auto-generated method stub
	}

}
