package com.bagri.xdm.client.hazelcast.impl;

import static com.bagri.xdm.client.hazelcast.serialize.DataSerializationFactoryImpl.factoryId;
import static com.bagri.xdm.cache.api.CacheConstants.PN_XDM_SCHEMA_POOL;
import static com.bagri.xdm.client.hazelcast.serialize.DataSerializationFactoryImpl.cli_QueuedCursor;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.xdm.api.XDMException;
import com.bagri.xdm.api.impl.ResultCursorBase;
import com.bagri.xdm.client.hazelcast.task.query.ResultFetcher;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.core.IQueue;
import com.hazelcast.core.Member;
import com.hazelcast.core.MemberSelector;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;

public class QueuedCursorImpl extends ResultCursorBase implements IdentifiedDataSerializable { 
	
    private static final transient Logger logger = LoggerFactory.getLogger(QueuedCursorImpl.class);

	private int batchSize;
	private int queueSize;
	private String clientId;
	private String memberId;
	private Object current;

	// server side
	private Iterator<Object> iter;
	
	private IQueue<Object> queue;
	private HazelcastInstance hzi;

	private MemberSelector selector = new ResultMemberSelector();
	
	public QueuedCursorImpl() {
		// for de-serializer
	}
	
	public QueuedCursorImpl(Iterator<Object> iter, String clientId, int batchSize) {
		this.iter = iter;
		this.clientId = clientId;
		this.batchSize = batchSize;
		this.queueSize = UNKNOWN;
	}

	public QueuedCursorImpl(Iterator<Object> iter, String clientId, int batchSize, int queueSize) {
		this(iter, clientId, batchSize);
		this.queueSize = queueSize;
	}
	
	@Override
	public void close() {
		logger.trace("close.enter; queue remaining size: {}", queue.size());
		queue.clear();
		iter = null; // on the server side
		current = null;
		//if (destroy) {
		//	queue.destroy();
		//}
	}

	protected Object getCurrent() {
		return current;
	}

	private IQueue<Object> getQueue() {
		if (queue == null) {
			queue = hzi.getQueue("client:" + clientId); 
		}
		return queue;
	}

	// client side
	public void deserialize(HazelcastInstance hzi) {
		this.hzi = hzi;
		queue = getQueue();
		current = null; 
		position = 0; //-1;
	}
	
	// server side
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
	
	@Override
	public List<?> getList() throws XDMException {
		throw new XDMException("Not implemented in queue", XDMException.ecQuery);
	}
	
	@Override
	public boolean next() {
		current = queue.poll();
		boolean result = current != null;
		if (!result) {
			if (position < queueSize || ((queueSize < EMPTY) && (position % batchSize) == 0)) {
				logger.debug("hasNext; got end of the queue; position: {}; queueSize: {}", position, queueSize);
				// request next batch from server side..
				IExecutorService exec = hzi.getExecutorService(PN_XDM_SCHEMA_POOL);
				Future<Boolean> fetcher = exec.submit(new ResultFetcher(clientId), selector);
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
		} else {
			position++;
		}
		logger.trace("hasNext; returning: {}", result); 
		return result;
	}

	@Override
	public int getFactoryId() {
		return factoryId;
	}

	@Override
	public int getId() {
		return cli_QueuedCursor;
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
	
	@Override
	public String toString() {
		return "ResultCursor [clientId=" + clientId + ", memberId=" + memberId + 
			", queueSize=" + queueSize + ", position=" + position + 
			", batchSize=" + batchSize + "]";
	}

	
	private class ResultMemberSelector implements MemberSelector {

		@Override
		public boolean select(Member member) {
			return memberId.equals(member.getUuid());
		}
		
	}

}
