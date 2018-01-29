package com.bagri.client.hazelcast.impl;

import static com.bagri.client.hazelcast.serialize.SystemSerializationFactory.cli_QueuedCursor;
import static com.bagri.client.hazelcast.serialize.SystemSerializationFactory.cli_factory_id;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import com.bagri.core.api.BagriException;
import com.bagri.core.api.impl.ResultCursorBase;
import com.bagri.core.model.Null;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IQueue;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;

public class QueuedCursorImpl<T> extends ResultCursorBase<T> implements IdentifiedDataSerializable { 
	
	private String queueName;
	protected IQueue queue;
	private HazelcastInstance hzi;

	public QueuedCursorImpl() {
		// for de-serializer
	}
	
	public QueuedCursorImpl(HazelcastInstance hzi, String queueName) {
		this.queueName = queueName;
		init(hzi);
	}
	
	@Override
	public boolean add(T result) {
		return queue.add(result);
	}
	
	@Override
	public void close() throws Exception {
		logger.trace("close.enter; queue remaining size: {}", queue.size());
		queue.clear();
		//queue.destroy();
	}

	@Override
	public void finish() {
		queue.add(Null._null);
	}
	
	//@Override
	public void init(HazelcastInstance hzi) {
		//logger.trace("init.enter; queue: {}", queueName);
		this.hzi = hzi;
		this.queue = hzi.getQueue(queueName);
	}

	@Override
	public boolean isEmpty() {
		// TODO implement it properly!?
		return queue.isEmpty(); 
	}

	@Override
	public Iterator<T> iterator() {
		return new QueuedCursorIterator<>(queue);
	}

	@Override
	public List<T> getList() throws BagriException {
		//return null;
		throw new BagriException("Not implemented in queued cursor", BagriException.ecInOut);
	}
	
	@Override
	public boolean isAsynch() {
		return true;
	}
	
	@Override
	public int getFactoryId() {
		return cli_factory_id;
	}

	@Override
	public int getId() {
		return cli_QueuedCursor;
	}

	@Override
	public int size() {
		return queue.size();
	}

	@Override
	public void readData(ObjectDataInput in) throws IOException {
		queueName = in.readUTF();
	}

	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		out.writeUTF(queueName);
	}
	
	@Override
	public String toString() {
		return "QueuedCursorImpl [queueName=" + queueName + "]";
	}
	
	
	protected static class QueuedCursorIterator<T> implements Iterator<T> {

		private T current;
		private IQueue queue;
		
		QueuedCursorIterator(IQueue queue) {
			this.queue = queue;
		}

		@Override
		public boolean hasNext() {
			try {
				current = (T) queue.take();
				if (current instanceof Null) {
					current = null;
				}
			} catch (InterruptedException e) {
				current = null;
			}
			return current != null;
		}

		@Override
		public T next() {
			return current;
		}
		
	}

}

