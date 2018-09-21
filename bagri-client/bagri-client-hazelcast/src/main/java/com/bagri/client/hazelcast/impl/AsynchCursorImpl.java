package com.bagri.client.hazelcast.impl;

import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.core.api.ResultCursor;
import com.hazelcast.core.ExecutionCallback;

public class AsynchCursorImpl<T> extends CombinedCursorImpl<T> implements ExecutionCallback<ResultCursor<T>> {

    private final static Logger logger = LoggerFactory.getLogger(AsynchCursorImpl.class);
	
	private int expected = 1;
	private AtomicInteger failures = new AtomicInteger(0);
	private AtomicInteger received = new AtomicInteger(0);
	
	public AsynchCursorImpl() {
		super();
	}
	
	public AsynchCursorImpl(int limit, int expected) {
		super(limit);
		this.expected = expected;
	}

	@Override
	public boolean isAsynch() {
		return true;
	}

	@Override
	public boolean isComplete() {
		int r = received.get();
		int f = failures.get();
		logger.trace("isComplete; expected: {}, received: {}, failures: {}", expected, r, f);
		return expected == r + f;
	}

	@Override
	public boolean isEmpty() {
		if (isComplete()) {
			return super.isEmpty();
		}
		return false;
	}

	@Override
	public void onResponse(ResultCursor<T> response) {
		addResults(response);
		received.incrementAndGet();
	}

	@Override
	public void onFailure(Throwable t) {
		failures.incrementAndGet();
	}

}
