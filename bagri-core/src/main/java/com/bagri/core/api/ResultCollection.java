package com.bagri.core.api;

public interface ResultCollection extends AutoCloseable, Iterable<Object> {
	
	//void init(SchemaRepository repo);
	
	boolean add(Object result);
	
	int size();

}
