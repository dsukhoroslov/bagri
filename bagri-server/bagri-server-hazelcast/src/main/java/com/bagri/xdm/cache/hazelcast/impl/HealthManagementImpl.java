package com.bagri.xdm.cache.hazelcast.impl;

import static com.bagri.xdm.client.common.XDMCacheConstants.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.xdm.api.XDMHealthState;
import com.bagri.xdm.common.XDMDocumentKey;
import com.bagri.xdm.domain.XDMCounter;
import com.bagri.xdm.domain.XDMDocument;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.ITopic;
import com.hazelcast.core.Message;
import com.hazelcast.core.MessageListener;
import com.hazelcast.partition.PartitionLostEvent;
import com.hazelcast.partition.PartitionLostListener;

public class HealthManagementImpl implements MessageListener<XDMCounter>, PartitionLostListener {
	//, XDMHealthManagement{ 

    private static final Logger logger = LoggerFactory.getLogger(HealthManagementImpl.class);

	private HazelcastInstance hzInstance;
	private ITopic<XDMHealthState> hTopic;
	private IMap<XDMDocumentKey, XDMDocument> xddCache;
	
	private int thLow = 10;
	private int thHigh = 0;
	
	private int cntActive = 0;
	private int cntInactive = 0;
	
	public HealthManagementImpl() {
		//super();
	}
	
	public HealthManagementImpl(HazelcastInstance hzInstance) {
		initialize(hzInstance);
	}
	
	public void setLowThreshold(int thLow) {
		this.thLow = thLow;
	}
	
	public void setHighThreshold(int thHigh) {
		this.thHigh = thHigh;
	}

	private void initialize(HazelcastInstance hzInstance) {
		this.hzInstance = hzInstance;
		ITopic<XDMCounter> cTopic = hzInstance.getTopic(TPN_XDM_COUNTERS);
		cTopic.addMessageListener(this);
		hTopic = hzInstance.getTopic(TPN_XDM_HEALTH);
		xddCache = hzInstance.getMap(CN_XDM_DOCUMENT);
		hzInstance.getPartitionService().addPartitionLostListener(this);
	}
	
	private void updateState(XDMCounter counter) {
		if (counter.isCommit()) {
			cntActive += counter.getCreated();
			cntActive -= counter.getDeleted();
			cntInactive += counter.getUpdated();
			cntInactive += counter.getDeleted(); 
		} else {
			// nothing will change, all garbage will be compensated
		}
		checkState();
	}
	
	private void checkState() {
		int docSize = xddCache.size();
		logger.trace("checkStats; active count: {}; inactive count: {}; cache size: {}", 
				cntActive, cntInactive, docSize);
		int fullSize = cntActive + cntInactive;
		XDMHealthState hState;
		if (fullSize < docSize - thLow) {
			hState = XDMHealthState.bad;
		} else if (fullSize > docSize + thHigh) {
			hState = XDMHealthState.ugly;
		} else {
			hState = XDMHealthState.good; 
		} 
		if (hState != XDMHealthState.good) { 			
			logger.info("checkState; the state is: {}; expected size: {}; cache size: {}", hState, fullSize, docSize);
		}
		hTopic.publish(hState);
	}
	
	@Override
	public void onMessage(Message<XDMCounter> message) {
		logger.trace("onMessage; {}", message); 
		updateState(message.getMessageObject());
	}

	@Override
	public void partitionLost(PartitionLostEvent event) {
		logger.debug("partitionLost; event: {}", event); 
		checkState();
	}
	
	public void clearState() {
		cntActive = 0;
		cntInactive = 0;
		hTopic.publish(XDMHealthState.good);
	}

	public void initState(int docActive, int docInactive) {
		cntActive = docActive;
		cntInactive = docInactive;
		checkState();
	}
	
/*		
	@Override
	public void checkClusterState() throws XDMException {
		// TODO Auto-generated method stub
	}

	@Override
	public boolean isClusterSafe() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int getClusterSize() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public XDMHealthState getHealthState() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addHealthChangeListener(XDMHealthChangeListener listener) {
		// TODO Auto-generated method stub
	}

	@Override
	public void removeHealthChangeListener(XDMHealthChangeListener listener) {
		// TODO Auto-generated method stub
	}
*/
}
