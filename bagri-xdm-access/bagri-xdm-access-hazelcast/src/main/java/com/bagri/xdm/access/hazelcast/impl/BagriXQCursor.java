package com.bagri.xdm.access.hazelcast.impl;

import java.util.Iterator;
import java.util.Properties;

import com.bagri.xquery.api.XQCursor;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IQueue;

public class BagriXQCursor implements XQCursor {
	
	private Iterator iter;
	private String queueName;
	
	private IQueue queue;
	
	//public BagriXQCursor() {
		//
		// create Iterator here..
	//}
	
	public BagriXQCursor(Iterator iter) {
		this.iter = iter;
	}

	public BagriXQCursor(String qName) {
		this.queueName = qName;
	}

	public void deserialize(HazelcastInstance hz) {
		queue = hz.getQueue(queueName);
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
	
	public Iterator getIterator() {
		return queue.iterator();
	}

	@Override
	public Object current() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasNext() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Object next() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Properties getProperties() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setProperties(Properties props) {
		// TODO Auto-generated method stub
		
	}

}
