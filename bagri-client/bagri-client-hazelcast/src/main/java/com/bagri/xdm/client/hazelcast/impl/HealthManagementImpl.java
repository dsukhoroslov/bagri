package com.bagri.xdm.client.hazelcast.impl;

import java.util.HashMap;
import java.util.Map;

import static com.bagri.xdm.client.common.XDMCacheConstants.TPN_XDM_HEALTH;

import com.bagri.xdm.api.XDMException;
import com.bagri.xdm.api.XDMHealthChangeListener;
import com.bagri.xdm.api.XDMHealthManagement;
import com.bagri.xdm.api.XDMHealthState;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ITopic;
import com.hazelcast.core.Message;
import com.hazelcast.core.MessageListener;

public class HealthManagementImpl implements XDMHealthManagement, MessageListener<XDMHealthState> {

	private HazelcastInstance hzInstance;
	private XDMHealthState state = XDMHealthState.bad;
	private Map<Integer, XDMHealthChangeListener> listeners = new HashMap<>();
	
	public HealthManagementImpl(HazelcastInstance hzInstance) {
		this.hzInstance = hzInstance;
		ITopic<XDMHealthState> hTopic = hzInstance.getTopic(TPN_XDM_HEALTH);
		hTopic.addMessageListener(this);
		// TODO: get initial state somehow!
	}
	
	public void checkClusterState() throws XDMException {
		if (!isClusterSafe()) {
			throw new XDMException("System is not healthy", XDMException.ecHealth);
		}
	}
	
	@Override
	public boolean isClusterSafe() {
		return state == XDMHealthState.good; 
	}

	@Override
	public int getClusterSize() {
		return hzInstance.getCluster().getMembers().size();
	}

	@Override
	public XDMHealthState getHealthState() {
		return state;
	}
	
	@Override
	public void addHealthChangeListener(XDMHealthChangeListener listener) {
		listeners.put(listener.hashCode(), listener);		
	}

	@Override
	public void removeHealthChangeListener(XDMHealthChangeListener listener) {
		listeners.remove(listener.hashCode());		
	}

	@Override
	public void onMessage(Message<XDMHealthState> message) {
		XDMHealthState newState = message.getMessageObject();
		for (XDMHealthChangeListener list: listeners.values()) {
			list.onHealthStateChange(newState);
		}
		this.state = newState;
	}


}
