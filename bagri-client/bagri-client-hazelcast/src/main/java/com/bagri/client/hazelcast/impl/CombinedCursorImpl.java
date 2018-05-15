package com.bagri.client.hazelcast.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;

import com.bagri.core.api.ResultCursor;

public class CombinedCursorImpl<T> implements ResultCursor<T> {
	
    private int limit;
	protected Collection<ResultCursor<T>> results = new ArrayList<>();
	
	public CombinedCursorImpl() {
		this(0);
	}
	
	public CombinedCursorImpl(int limit) {
		this.limit = limit;
	}

	@Override
	public void close() throws Exception {
		for (ResultCursor<T> rc: results) {
			rc.close();
		}
		//index = 0;
		// close iterators?
	}
	
	public void addResults(ResultCursor<T> result) {
		results.add(result);
	}

	@Override
	public boolean isAsynch() {
		return false;
	}

	@Override
	public boolean isComplete() {
		for (ResultCursor<T> cursor: results) {
			if (!cursor.isComplete()) {
				return false;
			}
		}
		return true;
	}
	
	@Override
	public boolean isEmpty() {
		for (ResultCursor<T> cursor: results) {
			if (!cursor.isEmpty()) {
				return false;
			}
		}
		return true;
	}

	@Override
	public Iterator<T> iterator() {
		return new CombinedCursorIterator<>(limit, results);
	}
	
	@Override
	public int size() {
		return UNKNOWN;
	}

	
	private static class CombinedCursorIterator<T> implements Iterator<T> {

		private int limit = 0;
		private int index = 0;
		private Iterator<T> curIter = null;
		private ResultCursor<T> curResult = null;
		private Deque<ResultCursor<T>> cursors = null;
		
		CombinedCursorIterator(int limit, Collection<ResultCursor<T>> results) {
			this.limit = limit;
			this.cursors = new LinkedList<>(results);
		}

		@Override
		public boolean hasNext() {
			if (limit > 0 && index >= limit) {
				return false;
			}
			if (cursors.isEmpty() && curResult == null) {
				return false;
			}
			if (curResult == null) {
				curResult = cursors.pop();
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
			if (curIter == null) {
				if (!hasNext()) {
					throw new NoSuchElementException("No more elements in the cursor");
				}
			}
			index++;
			return (T) curIter.next();
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException("remove() method not implemented");
		}
	
	}


}
