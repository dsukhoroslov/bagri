package com.bagri.client.hazelcast.impl;

import com.bagri.core.api.ResultCursor;
import com.hazelcast.core.ExecutionCallback;

public class AsynchCursorImpl<T> extends CombinedCursorImpl<T> implements ExecutionCallback<ResultCursor<T>> {

	private int failures = 0;
	private int received = 0;
	private int expected = 1;
	
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
	}

	@Override
	public void onFailure(Throwable t) {
		failures++;
	}

}
