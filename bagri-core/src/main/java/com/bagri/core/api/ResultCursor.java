package com.bagri.core.api;

import java.util.List;
import java.util.Properties;

import javax.xml.xquery.XQItemAccessor;

import org.w3c.dom.Node;

/**
 * Represents a cursor over (X-)Query results. Used for lazy result fetching from the server side
 * 
 * @author Denis Sukhoroslov
 *
 */
public interface ResultCursor extends AutoCloseable {
	
	/**
	 * 
	 * @return currently selected item as {@literal boolean} primitive
	 * @throws BagriException in case of conversion error
	 */
	boolean getBoolean() throws BagriException;
	
	/**
	 * 
	 * @return currently selected item as {@literal byte} primitive
	 * @throws BagriException in case of conversion error
	 */
	byte getByte() throws BagriException;
	
	/**
	 * 
	 * @return currencly selected item as {@literal double} primitive
	 * @throws BagriException in case of conversion error
	 */
	double getDouble() throws BagriException;
	
	/**
	 * 
	 * @return currently selected item as {@literal float} primitive
	 * @throws BagriException in case of conversion error
	 */
	float getFloat() throws BagriException;
	
	/**
	 * 
	 * @return currently selected item as {@literal int} primitive
	 * @throws BagriException in case of conversion error
	 */
	int getInt() throws BagriException;
	
	/**
	 * 
	 * @return currently selected item as {@literal long} primitive
	 * @throws BagriException in case of conversion error
	 */
	long getLong() throws BagriException;
	
	/**
	 * 
	 * @return currently selected item as XML Node
	 * @throws BagriException in case of conversion error
	 */
	Node getNode() throws BagriException;
	
	/**
	 * 
	 * @return currently selected item's value
	 * @throws BagriException in case of data access error
	 */
	Object getObject() throws BagriException;
	
	/**
	 * 
	 * @return currently selected item as {@literal short} primitive
	 * @throws BagriException in case of conversion error
	 */
	short getShort() throws BagriException;
	
	/**
	 * 
	 * @return currently selected item as String
	 * @throws BagriException in case of conversion error
	 */
	String getString() throws BagriException;
	
	/**
	 * 
	 * @return the underlying list of selected items
	 * @throws BagriException in case of list access error
	 */
	List<Object> getList() throws BagriException;
	
	/**
	 * 
	 * @return currently selected item itself
	 * @throws BagriException in case of data access error
	 */
	XQItemAccessor getXQItem() throws BagriException;

	/**
	 * 
	 * @param props result production properties
	 * @return transforms currently selected item to String
	 * @throws BagriException in case of data access error
	 */
	String getItemAsString(Properties props) throws BagriException;

	/**
	 * 
	 * @return true if cursor has a static List of results fetched, false otherwise 
	 */
	boolean isFixed();
	
	/**
	 * Moves selected item one position next  
	 * 
	 * @return true if next item selected, false otherwise
	 * @throws BagriException in case of data access error 
	 */
	boolean next() throws BagriException;
	
}
