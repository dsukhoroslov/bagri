package com.bagri.core.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public abstract class ResultCollection implements Iterable<Object> {
	
	protected ArrayList<Object> results = new ArrayList<>();
	
	public ResultCollection() {
		//
	}

	public ResultCollection(int size) {
		this.results.ensureCapacity(size);
	}
	
	public ResultCollection(Collection<Object> results) {
		this.results.ensureCapacity(results.size());
		this.results.addAll(results);
	}
	
	public boolean add(Object result) {
		return results.add(result);
	}
	
	@Override
	public Iterator<Object> iterator() {
		return results.iterator();
	}
	
	public int size() {
		return results.size();
	}

}
