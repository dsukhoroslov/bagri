package com.bagri.xdm.domain;

import java.util.Set;

/**
 * @author Denis Sukhoroslov
 * 
 */
public abstract class IndexedValue { 

	/**
	 * default constructor
	 */
	public IndexedValue() {
		// de-ser
	}

	/**
	 * 
	 * @return the number of indexed documents 
	 */
	public abstract int getCount();
	
	/**
	 * 
	 * @return the single internal document key
	 */
	public abstract long getDocumentKey();
	
	/**
	 * 
	 * @return indexed document keys
	 */
	public abstract Set<Long> getDocumentKeys();
	
	/**
	 * 
	 * @param docKey the document key to add to the index
	 * @param txId transaction id
	 * @return true if key was added, false otherwise
	 */
	public abstract boolean addDocument(long docKey, long txId);
	
	/**
	 * 
	 * @param docKey the document key to remove from the index
	 * @param txId transaction id
	 * @return true if key was removed, false otherwise
	 */
	public abstract boolean removeDocument(long docKey, long txId);
	
	/**
	 * 
	 * @return size of the index in bytes
	 */
	public abstract int getSize();

}
