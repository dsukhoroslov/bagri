package com.bagri.client.hazelcast.impl;

import static com.bagri.client.hazelcast.serialize.SystemSerializationFactory.cli_ResultCollection;
import static com.bagri.client.hazelcast.serialize.SystemSerializationFactory.cli_factory_id;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.core.api.ResultCollection;
import com.bagri.core.model.Null;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IQueue;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;

public class QueuedCollectionImpl extends ResultCollection implements Iterator<Object>, IdentifiedDataSerializable {  

    private final static Logger logger = LoggerFactory.getLogger(QueuedCollectionImpl.class);

	private String clientId;
	private String queueName;
	private Object current;

	// server side
	//private List<Object> results;
	//private Iterator<Object> iter;
	
	private IQueue<Object> queue;
	private HazelcastInstance hzi;
    
	public QueuedCollectionImpl() {
		super();
	}

	public QueuedCollectionImpl(HazelcastInstance hzi, String clientId, String queueName) {
		this.clientId = clientId;
		this.queueName = queueName;
		init(hzi);
	}
	
	void init(HazelcastInstance hzi) {
		this.hzi = hzi;
		logger.trace("init; client: {}; queue: {}", clientId, queueName);
		this.queue = hzi.getQueue(queueName);
	}

	@Override
	public int getFactoryId() {
		return cli_factory_id;
	}

	@Override
	public int getId() {
		return cli_ResultCollection;
	}

	@Override
	public boolean add(Object result) {
		return queue.add(result);
	}
	
	@Override
	public Iterator<Object> iterator() {
		return this;
		//return queue.iterator();
	}

	@Override
	public int size() {
		throw new UnsupportedOperationException("size() is not supported in the asynch collection impl");//return results.size();
	}
	
	@Override
	public void readData(ObjectDataInput in) throws IOException {
		clientId = in.readUTF();
		queueName = in.readUTF();
	}

	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		out.writeUTF(clientId);
		out.writeUTF(queueName);
	}

	@Override
	public boolean hasNext() {
		try {
			current = queue.take();
			if (current instanceof Null) {
				current = null;
			}
		} catch (InterruptedException e) {
			current = null;
		}
		return current != null;
	}

	@Override
	public Object next() {
		return current;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException("remove() is not supported");
	}


}
