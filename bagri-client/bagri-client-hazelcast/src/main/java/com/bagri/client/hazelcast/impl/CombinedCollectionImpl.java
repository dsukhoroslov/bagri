package com.bagri.client.hazelcast.impl;

import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;

import com.bagri.core.api.ResultCollection;

public class CombinedCollectionImpl<T> implements AutoCloseable, Iterable<T>, Iterator<T> {
	
	private Iterator<T> curIter = null;
	private ResultCollection<T> curResult = null;
	private Deque<ResultCollection<T>> results = new LinkedList<>();

	@Override
	public void close() throws Exception {
		for (ResultCollection<T> cln: results) {
			cln.close();
		}
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
		return curIter.next();
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException("remove() is not implemented"); 
	}


}
