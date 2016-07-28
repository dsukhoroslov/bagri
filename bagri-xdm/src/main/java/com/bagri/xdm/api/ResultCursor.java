package com.bagri.xdm.api;

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
	 * @throws XDMException in case of conversion error
	 */
	boolean getBoolean() throws XDMException;
	
	/**
	 * 
	 * @return currently selected item as {@literal byte} primitive
	 * @throws XDMException in case of conversion error
	 */
	byte getByte() throws XDMException;
	
	/**
	 * 
	 * @return currencly selected item as {@literal double} primitive
	 * @throws XDMException in case of conversion error
	 */
	double getDouble() throws XDMException;
	
	/**
	 * 
	 * @return currently selected item as {@literal float} primitive
	 * @throws XDMException in case of conversion error
	 */
	float getFloat() throws XDMException;
	
	/**
	 * 
	 * @return currently selected item as {@literal int} primitive
	 * @throws XDMException in case of conversion error
	 */
	int getInt() throws XDMException;
	
	/**
	 * 
	 * @return currently selected item as {@literal long} primitive
	 * @throws XDMException in case of conversion error
	 */
	long getLong() throws XDMException;
	
	/**
	 * 
	 * @return currently selected item as XML Node
	 * @throws XDMException in case of conversion error
	 */
	Node getNode() throws XDMException;
	
	/**
	 * 
	 * @return currently selected item's value
	 * @throws XDMException in case of data access error
	 */
	Object getObject() throws XDMException;
	
	/**
	 * 
	 * @return currently selected item as {@literal short} primitive
	 * @throws XDMException in case of conversion error
	 */
	short getShort() throws XDMException;
	
	/**
	 * 
	 * @return currently selected item as String
	 * @throws XDMException in case of conversion error
	 */
	String getString() throws XDMException;
	
	/**
	 * 
	 * @return the underlying list of selected items
	 * @throws XDMException in case of list access error
	 */
	List<Object> getList() throws XDMException;
	
	/**
	 * 
	 * @return currently selected item itself
	 * @throws XDMException in case of data access error
	 */
	XQItemAccessor getXQItem() throws XDMException;

	/**
	 * 
	 * @return transforms currently selected item to String
	 * @throws XDMException in case of data access error
	 */
	String getItemAsString(Properties props) throws XDMException;

	/**
	 * 
	 * @return true if cursor has a List of results fetched, false otherwise 
	 */
	boolean isFixed();
	
	/**
	 * Moves selected item one position next  
	 * 
	 * @return true if next item selected, false otherwise
	 * @throws XDMException in case of data access error 
	 */
	boolean next() throws XDMException;
	
}
