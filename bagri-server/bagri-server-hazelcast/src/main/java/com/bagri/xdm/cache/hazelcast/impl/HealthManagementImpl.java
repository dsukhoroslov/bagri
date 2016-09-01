package com.bagri.xdm.cache.hazelcast.impl;

import static com.bagri.xdm.cache.api.CacheConstants.*;

import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.xdm.api.HealthState;
import com.bagri.xdm.common.DocumentKey;
import com.bagri.xdm.domain.Counter;
import com.bagri.xdm.domain.Document;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.ITopic;
import com.hazelcast.core.Message;
import com.hazelcast.core.MessageListener;
import com.hazelcast.partition.PartitionLostEvent;
import com.hazelcast.partition.PartitionLostListener;

public class HealthManagementImpl implements MessageListener<Counter>, PartitionLostListener {
	//, XDMHealthManagement{ 

    private static final Logger logger = LoggerFactory.getLogger(HealthManagementImpl.class);

	private HazelcastInstance hzInstance;
	private ITopic<HealthState> hTopic;
	private IMap<DocumentKey, Document> xddCache;
	
	private HealthState healthState = HealthState.good; 
	
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
		ITopic<Counter> cTopic = hzInstance.getTopic(TPN_XDM_COUNTERS);
		cTopic.addMessageListener(this);
		ITopic<Long> pTopic = hzInstance.getTopic(TPN_XDM_POPULATION);
		pTopic.addMessageListener(new PopulationStateListener());
		hTopic = hzInstance.getTopic(TPN_XDM_HEALTH);
		xddCache = hzInstance.getMap(CN_XDM_DOCUMENT);
		hzInstance.getPartitionService().addPartitionLostListener(this);
	}
	
	private void updateState(Counter counter) {
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
		logger.trace("checkState; active count: {}; inactive count: {}; cache size: {}", cntActive, cntInactive, docSize);
		long fullSize = cntActive.get() + cntInactive.get();
		HealthState hState;
		if (fullSize < docSize - thLow) {
			hState = HealthState.bad;
		} else if (fullSize > docSize + thHigh) {
			hState = HealthState.ugly;
		} else {
			hState = HealthState.good; 
		} 
		if (healthState != hState) {
			healthState = hState;
			hTopic.publish(hState);
			if (healthState != HealthState.good) { 			
				logger.info("checkState; the state is: {}; expected size: {}; cache size: {}", hState, fullSize, docSize);
			}
		}
	}
	
	public void clearState() {
		cntActive = new AtomicLong(0);
		cntInactive = new AtomicLong(0);
		hTopic.publish(HealthState.good);
	}

	public int[] getCounters() {
		return new int[] {cntActive.intValue(), cntInactive.intValue()};
	}
	
	public HealthState getHealthState() {
		return healthState;
	}

	public void initState(int docActive, int docInactive) {
		cntActive = new AtomicLong(docActive);
		cntInactive = new AtomicLong(docInactive);
		checkState();
	}
	
	@Override
	public void onMessage(Message<Counter> message) {
		logger.trace("onMessage; {}", message); 
		updateState(message.getMessageObject());
	}

	@Override
	public void partitionLost(PartitionLostEvent event) {
		logger.debug("partitionLost; event: {}", event); 
		checkState();
	}
	
	private class PopulationStateListener implements MessageListener<Long> {

		@Override
		public void onMessage(Message<Long> message) {
			int lo = (int) message.getMessageObject().longValue();
			int hi = (int) (message.getMessageObject().longValue() >> 32);
			initState(lo, hi);
			// now we can disconnect, actually..
		}
		
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
