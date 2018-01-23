package com.bagri.core.api;

/**
 * Provides an ability to asynchronously fetch selected Documents from client side
 * 
 * @author Denis Sukhoroslov
 */
public interface ResultCollection extends AutoCloseable, Iterable<DocumentAccessor> {
	
	//void init(SchemaRepository repo);
	
	boolean add(DocumentAccessor result);
	
	void finish();
	
	boolean isAsynch();
	
	int size();

}
