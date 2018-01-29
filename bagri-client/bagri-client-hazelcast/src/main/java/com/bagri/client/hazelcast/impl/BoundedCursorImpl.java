package com.bagri.client.hazelcast.impl;

import static com.bagri.client.hazelcast.serialize.SystemSerializationFactory.cli_BoundedCursor;

import java.io.IOException;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.client.hazelcast.impl.QueuedCursorImpl.QueuedCursorIterator;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IQueue;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;

public class BoundedCursorImpl<T> extends QueuedCursorImpl<T> {
	
    //private final static Logger logger = LoggerFactory.getLogger(BoundedCursorImpl.class);

	private int limit;
	
	public BoundedCursorImpl() {
		super();
	}
	
	public BoundedCursorImpl(HazelcastInstance hzi, String queueName, int limit) {
		super(hzi, queueName);
		if (limit <= 0) {
			throw new IllegalArgumentException("collection limit must be greated than 0");
		}
		this.limit = limit;
	}
	
	//@Override
	//public void close() throws Exception {
	//	super.close();
		// close iterators!
	//	index = 0;
	//}

	@Override
	public int getId() {
		return cli_BoundedCursor;
	}

	//@Override
	//public boolean add(DocumentAccessor result) {
	//	int size = queue.size();
	//	if (size < limit) {
	//		return queue.add(result);
	//	}
	//	logger.debug("add; queue {} is full: {}; limit: {}", queueName, size, limit);
	//	return false;
	//}
	
	@Override
	public Iterator<T> iterator() {
		return new BoundedCursorIterator<>(queue, limit);
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

	
	protected static class BoundedCursorIterator<T> extends QueuedCursorIterator<T> {

		private int limit;
		private int index = 0;
		
		BoundedCursorIterator(IQueue<T> queue, int limit) {
			super(queue);
			this.limit = limit;
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
	}
}
