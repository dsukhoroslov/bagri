package com.bagri.client.hazelcast.impl;

import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;

import com.bagri.core.api.ResultCollection;

public class CombinedCollectionImpl<T> implements AutoCloseable, Iterable<T>, Iterator<T> {
	
	private int limit;
	private int index = 0;
	private Iterator<T> curIter = null;
	private ResultCollection<T> curResult = null;
	private Deque<ResultCollection<T>> results = new LinkedList<>();
	
	public CombinedCollectionImpl() {
		this(0);
	}
	
	public CombinedCollectionImpl(int limit) {
		this.limit = limit;
	}

	@Override
	public void close() throws Exception {
		for (ResultCollection<T> cln: results) {
			cln.close();
		}
		index = 0;
	}
	
	public void addResults(ResultCollection<T> result) {
		results.add(result);
	}

	@Override
	public Iterator<T> iterator() {
		return this;
	}

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
		return curIter.next();
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException("remove() is not implemented"); 
	}


}
