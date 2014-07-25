package com.bagri.xdm.access.hazelcast.impl;

import java.util.Iterator;
import java.util.Properties;

import javax.xml.xquery.XQItem;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//import com.bagri.xquery.api.XQCursor;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IQueue;

public class HazelcastXQCursor implements Iterator {
	
    private static final transient Logger logger = LoggerFactory.getLogger(HazelcastXQCursor.class);
	
	private Iterator<XQItem> iter;
	private String queueName;
	
	private IQueue<XQItem> queue;
	
	public HazelcastXQCursor(Iterator<XQItem> iter) {
		this.iter = iter;
	}

	public HazelcastXQCursor(String qName) {
		this.queueName = qName;
	}

	public void deserialize(HazelcastInstance hz) {
		queue = hz.getQueue(queueName);
		iter = queue.iterator();
	}
	
	public void serialize(HazelcastInstance hz) {
		String qName = "temp_queue";
		queue = hz.getQueue(qName);
		while (iter.hasNext()) {
			queue.add(iter.next());
		}
	}
	
	public String getQueueName() {
		return queue.getName();
	}
	
	@Override
	public boolean hasNext() {
		boolean result = iter.hasNext();
		logger.trace("hasNext; returning: {}", result); 
		if (!result) {
			queue.destroy();
		}
		return result;
	}

	@Override
	public XQItem next() {
		XQItem result = iter.next();
		logger.trace("next; returning: {}", result);
		if (result == null) {
			queue.destroy();
		}
		return result;
	}

	@Override
	public void remove() {
		throw new RuntimeException("metod remove not implemented");
	}
	
	//@Override
	public void close() {
		// TODO Auto-generated method stub
		queue.destroy();
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


}
