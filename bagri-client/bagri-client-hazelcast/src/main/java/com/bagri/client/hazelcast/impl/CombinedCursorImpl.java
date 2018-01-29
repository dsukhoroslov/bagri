package com.bagri.client.hazelcast.impl;

import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;

import com.bagri.core.api.ResultCursor;

public class CombinedCursorImpl<T> implements ResultCursor<T> {
	
	private int limit;
	private int index = 0;
	private ResultCursor<T> curResult = null;
	private Iterator<T> curIter = null;
	private Deque<ResultCursor<T>> results = new LinkedList<>();
	
	public CombinedCursorImpl() {
		this(0);
	}
	
	public CombinedCursorImpl(int limit) {
		this.limit = limit;
	}

	@Override
	public void close() throws Exception {
		for (ResultCursor<T> cln: results) {
			cln.close();
		}
		index = 0;
	}
	
	public void addResults(ResultCursor<T> result) {
		results.add(result);
	}

	@Override
	public boolean isAsynch() {
		return true;
	}

	@Override
	public boolean isEmpty() {
		for (ResultCursor<T> cursor: results) {
			if (!cursor.isEmpty()) {
				return false;
			}
		}
		return false;
	}

	@Override
	public Iterator<T> iterator() {
		return new CombinedCursorIterator<T>();
	}
	
	@Override
	public int size() {
		return UNKNOWN;
	}

	
	private class CombinedCursorIterator<T> implements Iterator<T> {

		@Override
		public boolean hasNext() {
			if (limit > 0 && index >= limit) {
				return false;
			}
			if (results.isEmpty() && curResult == null) {
				return false;
			}
			if (curResult == null) {
				curResult = results.pop();
				curIter = curResult.iterator();
			}
			if (curIter.hasNext()) {
				return true;
			} else {
				try {
					curResult.close();
				} catch (Exception ex) {
					// unexpected..
				}
				curResult = null;
				return hasNext();
			}
		}
	
		@Override
		public T next() {
			index++;
			return (T) curIter.next();
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException("remove() method not implemented");
		}
	
	}


}
