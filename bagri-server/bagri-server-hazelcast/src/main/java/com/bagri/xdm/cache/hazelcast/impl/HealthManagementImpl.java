package com.bagri.xdm.cache.hazelcast.impl;

import static com.bagri.xdm.cache.api.XDMCacheConstants.*;

import java.util.concurrent.atomic.AtomicLong;

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
	
	private XDMHealthState healthState = XDMHealthState.good; 
	
	private int thLow = 10;
	private int thHigh = 0;
	
	// TODO: would be better to have an internal atomic structure 
	// which will update both fields in one atomic operation
	private AtomicLong cntActive = new AtomicLong(0);
	private AtomicLong cntInactive = new AtomicLong(0);
	
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
			cntActive.addAndGet(counter.getCreated() - counter.getDeleted());
			cntInactive.addAndGet(counter.getUpdated() + counter.getDeleted()); 
		} else {
			// nothing will change, all garbage will be compensated
		}
		checkState();
	}
	
	private void checkState() {
		int docSize = xddCache.size();
		logger.trace("checkStats; active count: {}; inactive count: {}; cache size: {}", cntActive, cntInactive, docSize);
		long fullSize = cntActive.get() + cntInactive.get();
		XDMHealthState hState;
		if (fullSize < docSize - thLow) {
			hState = XDMHealthState.bad;
		} else if (fullSize > docSize + thHigh) {
			hState = XDMHealthState.ugly;
		} else {
			hState = XDMHealthState.good; 
		} 
		if (healthState != hState) {
			healthState = hState;
			hTopic.publish(hState);
			if (healthState != XDMHealthState.good) { 			
				logger.info("checkState; the state is: {}; expected size: {}; cache size: {}", hState, fullSize, docSize);
			}
		}
	}
	
	public void clearState() {
		cntActive = new AtomicLong(0);
		cntInactive = new AtomicLong(0);
		hTopic.publish(XDMHealthState.good);
	}

	public int[] getCounters() {
		return new int[] {cntActive.intValue(), cntInactive.intValue()};
	}
	
	public XDMHealthState getHealthState() {
		return healthState;
	}

	public void initState(int docActive, int docInactive) {
		cntActive = new AtomicLong(docActive);
		cntInactive = new AtomicLong(docInactive);
		checkState();
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
