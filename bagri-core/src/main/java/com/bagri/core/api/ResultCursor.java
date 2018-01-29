package com.bagri.core.api;

/**
 * Represents a cursor over (X-)Query results or Document handling results. Used for lazy result fetching from the server side
 * 
 * @author Denis Sukhoroslov
 *
 */
public interface ResultCursor<T> extends AutoCloseable, Iterable<T> {
	
	/**
	 * 
	 * @return true if cursor has a static List of results fetched, false otherwise 
	 */
	boolean isAsynch();
	
	/**
	 * 
	 * @return true if cursor is empty, false otherwise
	 */
	boolean isEmpty();

	//void init(SchemaRepository repo);
	
	/**
	 * 
	 * @return number of results in the cursor
	 */
	int size();
	
}
