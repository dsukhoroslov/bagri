package com.bagri.client.hazelcast.impl;

import static com.bagri.client.hazelcast.serialize.SystemSerializationFactory.cli_QueuedCursor;
import static com.bagri.client.hazelcast.serialize.SystemSerializationFactory.cli_factory_id;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	@SuppressWarnings("unchecked")
	public boolean add(T result) {
		boolean added = queue.offer(result);
		logger.trace("add; added: {}", added);
		return added;
	}
	
	@Override
	public void close() throws Exception {
		logger.trace("close.enter; queue remaining size: {}", queue.size());
		queue.clear();
		//queue.destroy();
	}

	@Override
	@SuppressWarnings("unchecked")
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
	public List<T> getList() {
		//return null;
		throw new UnsupportedOperationException("Not implemented in queued cursor");
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

	    private static final Logger log = LoggerFactory.getLogger(QueuedCursorImpl.class);
		
		private T current;
		private IQueue queue;
		
		QueuedCursorIterator(IQueue queue) {
			this.queue = queue;
		}

		@Override
		@SuppressWarnings("unchecked")
		public boolean hasNext() {
			try {
				current = (T) queue.take();
				if (current instanceof Null) {
					current = null;
				}
			} catch (InterruptedException e) {
				current = null;
			}
			log.trace("hasNext; current: {}", current);
			return current != null;
		}

		@Override
		public T next() {
			return current;
		}
		
		@Override
		public void remove() {
			throw new UnsupportedOperationException("remove() method not implemented");
		}
	
	}

}

