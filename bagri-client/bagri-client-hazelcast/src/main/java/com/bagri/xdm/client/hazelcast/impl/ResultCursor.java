package com.bagri.xdm.client.hazelcast.impl;

import static com.bagri.xdm.client.common.XDMCacheConstants.PN_XDM_SCHEMA_POOL;
import static com.bagri.xdm.client.hazelcast.serialize.XDMDataSerializationFactory.factoryId;
import static com.bagri.xdm.client.hazelcast.serialize.XDMDataSerializationFactory.cli_XQCursor;

import java.io.IOException;
import java.util.Iterator;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.xdm.client.hazelcast.task.query.ResultFetcher;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.core.IQueue;
import com.hazelcast.core.Member;
import com.hazelcast.core.MemberSelector;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;

public class ResultCursor implements Iterator<Object>, IdentifiedDataSerializable, MemberSelector {
	
	public static final int ONE = 1;
	public static final int EMPTY = 0;
	public static final int ONE_OR_MORE = -1;
	public static final int UNKNOWN = -2;
	
    private static final transient Logger logger = LoggerFactory.getLogger(ResultCursor.class);

	private String clientId;
	private String memberId;
	//private boolean failure;
	private int batchSize;
	private int queueSize;
	protected int position;
	private IQueue<Object> queue;
	private HazelcastInstance hzi;

	// server side
	private Iterator<Object> iter;

	// client side
	private Object current = null;
	
	public ResultCursor() {
		// for de-serializer
	}
	
	public ResultCursor(String clientId, int batchSize, Iterator<Object> iter) {
		this.clientId = clientId;
		this.batchSize = batchSize;
		this.iter = iter;
		this.queueSize = UNKNOWN;
		//this.failure = false;
	}

	public ResultCursor(String clientId, int batchSize, Iterator<Object> iter, int queueSize) {
		this(clientId, batchSize, iter);
		this.queueSize = queueSize;
	}

	private IQueue<Object> getQueue() {
		if (queue == null) {
			queue = hzi.getQueue("client:" + clientId); 
		}
		return queue;
	}

	public void deserialize(HazelcastInstance hzi) {
		this.hzi = hzi;
		queue = getQueue();
		current = queue.poll();
		position = 0;
	}
	
	public int serialize(HazelcastInstance hzi) {
		this.hzi =  hzi;
		memberId = hzi.getCluster().getLocalMember().getUuid();
		queue = getQueue();
		int size = 0;
		if (batchSize > 0) {
			for (int i = 0; i < batchSize && addNext(); i++) {
				size++;
			}
			if (queueSize < EMPTY) {
				if (size > 0) {
					if (iter.hasNext()) {
						queueSize = ONE_OR_MORE;
					} else {
						queueSize = ONE; 
					}
				} else {
					queueSize = EMPTY;
				}
			} else if (size > queueSize) {
				logger.info("serialize; declared and current batch queue sizes do not match: {}/{}", queueSize, size);
				//queueSize = size; ?? 
			}
		} else {
			while (iter.hasNext()) { 
				addNext();
				size++;
			}
			if (queueSize < EMPTY) {
				queueSize = size;
			} else if (size != queueSize) {
				logger.info("serialize; declared and current queue sizes do not match: {}/{}", queueSize, size);
			}
		}
		return size;
	}
	
	private boolean addNext() {
		if (iter.hasNext()) {
			Object o = iter.next();
			logger.trace("addNext; next: {}", o);
			if (o != null) {
				if (queue.offer(o)) {
					position++;
					return true;
				} else {
					logger.warn("addNext; queue is full!");
				}
			}
		}
		return false;
	}
	
	public String getClientId() {
		return clientId;
	}
	
	public int getQueueSize() {
		return queueSize;
	}
	
	@Override
	public boolean hasNext() {
		boolean result = current != null;
		if (!result) {
			if (position < queueSize || ((queueSize < EMPTY) && (position % batchSize) == 0)) {
				logger.debug("hasNext; got end of the queue; position: {}; queueSize: {}", position, queueSize);
				// request next batch from server side..
				IExecutorService exec = hzi.getExecutorService(PN_XDM_SCHEMA_POOL);
				Future<Boolean> fetcher = exec.submit(new ResultFetcher(clientId), this);
				try {
					if (fetcher.get()) {
						current = queue.poll();
						result = current != null;
						if (!result && position < queueSize) {
							logger.warn("hasNext; declared and fetched queue sizes do not match: {}/{}", queueSize, position);
						}
					}
				} catch (InterruptedException | ExecutionException ex) {
					logger.error("hasNext.error", ex); 
				}
			}
		}
		logger.trace("hasNext; returning: {}", result); 
		return result;
	}

	@Override
	public Object next() {
		Object result = current;
		if (current != null) {
			current = queue.poll();
			position++;
		}
		logger.trace("next; returning: {}", result);
		return result;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException("remove not supported");
	}
	
	@Override
	public boolean select(Member member) {
		return memberId.equals(member.getUuid());
	}

	//@Override
	public void close(boolean destroy) {
		logger.trace("close.enter; queue remaining size: {}", queue.size());
		queue.clear();
		if (destroy) {
			queue.destroy();
		}
	}

	@Override
	public String toString() {
		return "ResultCursor [clientId=" + clientId + ", memberId=" + memberId + 
			", queueSize=" + queueSize + ", position=" + position + 
			", batchSize=" + batchSize + "]";
	}

	@Override
	public int getFactoryId() {
		return factoryId;
	}

	@Override
	public int getId() {
		return cli_XQCursor;
	}

	@Override
	public void readData(ObjectDataInput in) throws IOException {
		clientId = in.readUTF();
		queueSize = in.readInt();
		memberId = in.readUTF();
		batchSize = in.readInt();
	}

	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		out.writeUTF(clientId);
		out.writeInt(queueSize);
		out.writeUTF(memberId);
		out.writeInt(batchSize);
	}

}
