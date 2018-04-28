package com.bagri.client.hazelcast.impl;

import java.util.Map;

import com.bagri.core.api.ResultCursor;
import com.hazelcast.core.Member;
import com.hazelcast.core.MultiExecutionCallback;

public class AsynchCursorImpl<T> extends CombinedCursorImpl<T> implements MultiExecutionCallback {
	
	private boolean complete = false;
	
	public AsynchCursorImpl() {
		super();
	}
	
	public AsynchCursorImpl(int limit) {
		super(limit);
	}
	
	@Override
	public boolean isAsynch() {
		return true;
	}

	@Override
	public boolean isComplete() {
		return complete;
	}

	@Override
	public boolean isEmpty() {
		if (complete) {
			return super.isEmpty();
		}
		return false;
	}

	@Override
	public void onResponse(Member member, Object value) {
		addResults((ResultCursor<T>) value); 
	}

	@Override
	public void onComplete(Map<Member, Object> values) {
		complete = true;
	}

}
