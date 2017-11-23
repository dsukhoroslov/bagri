package com.bagri.client.hazelcast.impl;

import static com.bagri.client.hazelcast.serialize.SystemSerializationFactory.cli_QueuedCollection;
import static com.bagri.client.hazelcast.serialize.SystemSerializationFactory.cli_factory_id;

import java.io.IOException;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.core.api.ResultCollection;
import com.bagri.core.api.SchemaRepository;
import com.bagri.core.model.Null;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IQueue;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;

public class QueuedCollectionImpl<T> implements Iterator<T>, ResultCollection<T>, IdentifiedDataSerializable {  

    private final static Logger logger = LoggerFactory.getLogger(QueuedCollectionImpl.class);

	private int limit;
	private int index = 0;
	private String queueName;
	private Object current;

	private IQueue<Object> queue;
	private HazelcastInstance hzi;
    
	public QueuedCollectionImpl() {
		//
	}

	public QueuedCollectionImpl(HazelcastInstance hzi, String queueName, int limit) {
		this.queueName = queueName;
		this.limit = limit;
		init(hzi);
	}
	
	//@Override
	public void init(HazelcastInstance hzi) {
		//logger.trace("init.enter; queue: {}", queueName);
		this.hzi = hzi;
		this.queue = hzi.getQueue(queueName);
	}

	@Override
	public void close() throws Exception {
		queue.clear();		
		index = 0;
	}

	@Override
	public int getFactoryId() {
		return cli_factory_id;
	}

	@Override
	public int getId() {
		return cli_QueuedCollection;
	}

	@Override
	public boolean add(T result) {
		if (limit > 0) {
			if (queue.size() < limit) {
				return queue.add(result);
			}
			logger.trace("add; queue is full: {}; limit: {}", queue.size(), limit);
			return false;
		}
		return queue.add(result);
	}
	
	@Override
	public void finish() {
		queue.add(Null._null);
	}
	
	@Override
	public boolean isAsynch() {
		return true;
	}
	
	@Override
	public Iterator<T> iterator() {
		return this;
		//return queue.iterator();
	}

	@Override
	public int size() {
		throw new UnsupportedOperationException("size() is not supported in the asynch collection impl");
	}
	
	@Override
	public boolean hasNext() {
		if (limit > 0 && index >= limit) {
			return false;
		}
		try {
			current = queue.take();
			if (current instanceof Null) {
				current = null;
			} else {
				index++;
			}
		} catch (InterruptedException e) {
			current = null;
		}
		return current != null;
	}

	@Override
	public T next() {
		return (T) current;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException("remove() is not supported");
	}


	@Override
	public void readData(ObjectDataInput in) throws IOException {
		limit = in.readInt();
		queueName = in.readUTF();
	}

	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		out.writeInt(limit);
		out.writeUTF(queueName);
	}

}
