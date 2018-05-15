package com.bagri.client.hazelcast.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.core.api.ResultCursor;
import com.hazelcast.core.ExecutionCallback;

public class AsynchCursorImpl<T> extends CombinedCursorImpl<T> implements ExecutionCallback<ResultCursor<T>> {

    private final static Logger logger = LoggerFactory.getLogger(AsynchCursorImpl.class);
	
	private int expected = 1;
	private volatile int failures = 0;
	private volatile int received = 0;
	
	public AsynchCursorImpl() {
		super();
	}
	
	public AsynchCursorImpl(int limit, int expected) {
		super(limit);
		this.expected = expected;
	}

	@Override
	public void addResults(ResultCursor<T> result) {
		synchronized (results) {
			results.add(result);
		}
	}

	@Override
	public boolean isAsynch() {
		return true;
	}

	@Override
	public boolean isComplete() {
		//logger.trace("isComplete; expected: {}; received: {}; failures: {}", expected, received, failures);
		return expected == received + failures;
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
		received++;
		logger.trace("onResponse; expected: {}; received: {}; failures: {}", expected, received, failures);
	}

	@Override
	public void onFailure(Throwable t) {
		failures++;
		logger.trace("onFailure; expected: {}; received: {}; failures: {}", expected, received, failures);
	}

}
