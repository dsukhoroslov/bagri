package com.bagri.xdm.access.hazelcast.impl;

import java.util.Iterator;
import java.util.Properties;

import javax.xml.xquery.XQItem;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



//import com.bagri.xquery.api.XQCursor;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IQueue;

public class HazelcastXQCursor implements Iterator<Object> {
	
    private static final transient Logger logger = LoggerFactory.getLogger(HazelcastXQCursor.class);
	
	private Iterator<Object> iter;
	private Object current = null;

	private IQueue<Object> queue;
	private String clientId;
	private int batchSize;
	private boolean failure;
	
	// used to instantiate cursor from server side -> write
	public HazelcastXQCursor(String clientId, int batchSize, Iterator<Object> iter) {
		this.clientId = clientId;
		this.batchSize = batchSize;
		this.iter = iter;
		this.failure = false;
	}

	public HazelcastXQCursor(String clientId, int batchSize, Iterator<Object> iter, boolean failure) {
		this(clientId, batchSize, iter);
		this.failure = failure;
	}

	// used to instantiate cursor from client side -> read
	public HazelcastXQCursor(String clientId) { //, int qSize) {
		this.clientId = clientId;
		this.failure = false;
		//this.queueSize = qSize;
	}

	public HazelcastXQCursor(String clientId, boolean failure) { //, int qSize) {
		this(clientId);
		this.failure = failure;
	}

	public void deserialize(HazelcastInstance hz) {
		queue = hz.getQueue("client:" + clientId);
		current = queue.poll();
	}
	
	public void serialize(HazelcastInstance hz) {
		queue = hz.getQueue("client:" + clientId);
		if (batchSize > 0) {
			for (int i = 0; i < batchSize && iter.hasNext(); i++) {
				//queue.add(iter.next());
				addNext();
			}
			
			// we should store current position in iter, to repeat
			// results provision later..
		} else {
			while (iter.hasNext()) { 
				//queue.add(iter.next());
				addNext();
			}
		}
	}
	
	private void addNext() {
		Object o = iter.next();
		logger.trace("addNext; next: {}", o);
		if (o != null) {
			queue.add(o);
		}
	}
	
	public String getClientId() {
		return clientId;
	}
	
	//public int getQueueSize() {
	//	return queueSize;
	//}
	
	public boolean isFailure() {
		return failure;
	}
	
	@Override
	public boolean hasNext() {
		boolean result = current != null;
		logger.trace("hasNext; returning: {}", result); 
		return result;
	}

	@Override
	public Object next() {
		Object result = current;
		if (current != null) {
			current = queue.poll();
		}
		logger.trace("next; returning: {}", result);
		return result;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException("remove not supported");
	}
	
	//@Override
	public void close(boolean destroy) {
		logger.trace("close.enter; queue remaining size: {}", queue.size());
		queue.clear();
		if (destroy) {
			queue.destroy();
		}
	}

	//@Override
	public Properties getProperties() {
		// TODO Auto-generated method stub
		return null;
	}

	//@Override
	public void setProperties(Properties props) {
		// TODO Auto-generated method stub
	}

	@Override
	public String toString() {
		return "HazelcastXQCursor [clientId=" + clientId + ", failure="	+ failure + "]";
	}


}
