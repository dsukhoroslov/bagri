package com.bagri.core.api;

public interface ResultCollection<T> extends AutoCloseable, Iterable<T> {
	
	//void init(SchemaRepository repo);
	
	boolean add(T result);
	
	int size();

}
