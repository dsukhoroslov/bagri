package com.bagri.client.hazelcast.impl;

import static com.bagri.client.hazelcast.serialize.SystemSerializationFactory.cli_BoundedQueueCollection;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.core.api.DocumentAccessor;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;

public class BoundedQueueCollectionImpl extends QueuedCollectionImpl {
	
    private final static Logger logger = LoggerFactory.getLogger(BoundedQueueCollectionImpl.class);

	private int limit;
	private int index = 0;
	
	public BoundedQueueCollectionImpl() {
		super();
	}
	
	public BoundedQueueCollectionImpl(HazelcastInstance hzi, String queueName, int limit) {
		super(hzi, queueName);
		if (limit <= 0) {
			throw new IllegalArgumentException("collection limit must be greated than 0");
		}
		this.limit = limit;
	}
	
	@Override
	public void close() throws Exception {
		super.close();		
		index = 0;
	}

	@Override
	public int getId() {
		return cli_BoundedQueueCollection;
	}

	@Override
	public boolean add(DocumentAccessor result) {
		int size = queue.size();
		if (size < limit) {
			return queue.add(result);
		}
		logger.debug("add; queue {} is full: {}; limit: {}", queueName, size, limit);
		return false;
	}
	
	@Override
	public boolean hasNext() {
		if (index >= limit) {
			return false;
		}
		if (super.hasNext()) {
			index++;
			return true;
		}
		return false;
	}


	@Override
	public void readData(ObjectDataInput in) throws IOException {
		super.readData(in);
		limit = in.readInt();
	}

	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		super.writeData(out);
		out.writeInt(limit);
	}

}
