package com.bagri.xdm.client.hazelcast.impl;

import static com.bagri.xdm.cache.api.CacheConstants.TPN_XDM_HEALTH;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.xdm.api.XDMException;
import com.bagri.xdm.api.HealthChangeListener;
import com.bagri.xdm.api.HealthCheckState;
import com.bagri.xdm.api.HealthManagement;
import com.bagri.xdm.api.HealthState;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ITopic;
import com.hazelcast.core.Message;
import com.hazelcast.core.MessageListener;

public class HealthManagementImpl implements HealthManagement, MessageListener<HealthState> {

    private final static Logger logger = LoggerFactory.getLogger(HealthManagementImpl.class);
	
	private HazelcastInstance hzInstance;
	private HealthState state = HealthState.good;
	private HealthCheckState checkState = HealthCheckState.log;
	private Map<Integer, HealthChangeListener> listeners = new HashMap<>();
	
	public HealthManagementImpl() {
		super();
	}
	
	public HealthManagementImpl(HazelcastInstance hzInstance) {
		this.hzInstance = hzInstance;
		ITopic<HealthState> hTopic = hzInstance.getTopic(TPN_XDM_HEALTH);
		hTopic.addMessageListener(this);
		// TODO: get initial state somehow!
	}
	
	public void checkClusterState() throws XDMException {
		if (checkState != HealthCheckState.skip) {
			if (!isClusterSafe()) {
				if (checkState == HealthCheckState.raise) {
					throw new XDMException("System is not healthy", XDMException.ecHealth);
				} else {
					// log unhealthy time here?
					logger.warn("System is not healthy");
				}
			}
		}
	}
	
	@Override
	public boolean isClusterSafe() {
		return state == HealthState.good; 
	}

	@Override
	public int getClusterSize() {
		return hzInstance.getCluster().getMembers().size();
	}

	@Override
	public HealthState getHealthState() {
		return state;
	}
	
	public HealthCheckState getCheckState() {
		return checkState;
	}
	
	public void setCheckSate(HealthCheckState state) {
		this.checkState = state;
	}
	
	@Override
	public void addHealthChangeListener(HealthChangeListener listener) {
		listeners.put(listener.hashCode(), listener);
	}

	@Override
	public void removeHealthChangeListener(HealthChangeListener listener) {
		listeners.remove(listener.hashCode());		
	}

	@Override
	public void onMessage(Message<HealthState> message) {
		HealthState newState = message.getMessageObject();
		if (state != newState) {
			for (HealthChangeListener list: listeners.values()) {
				list.onHealthStateChange(newState);
			}
			logger.trace("onMessage; health state changed from {} to {}; listeners notified: {}", 
					state, newState, listeners.size()); 
		}
		state = newState;
	}


}
