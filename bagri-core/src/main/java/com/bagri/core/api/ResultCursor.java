package com.bagri.core.api;

/**
 * Represents a cursor over (X-)Query results or Document handling results. Used for lazy result fetching from the server side
 * 
 * @author Denis Sukhoroslov
 *
 */
public interface ResultCursor<T> extends AutoCloseable, Iterable<T> {
	
	public static final int ONE = 1;
	public static final int EMPTY = 0;
	public static final int ONE_OR_MORE = -1;
	public static final int UNKNOWN = -2;
	
	/**
	 * 
	 * @return true if cursor has a static List of results fetched, false otherwise 
	 */
	boolean isAsynch();
	
	/**
	 * 
	 * @return true if asynch cursor is fetched completely, false otherwise
	 */
	boolean isComplete();

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
