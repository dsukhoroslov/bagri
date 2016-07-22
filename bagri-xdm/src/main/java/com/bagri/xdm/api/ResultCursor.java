package com.bagri.xdm.api;

import java.util.List;

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
	 * @throws XDMException
	 */
	boolean getBoolean() throws XDMException;
	
	/**
	 * 
	 * @return currently selected item as {@literal byte} primitive
	 * @throws XDMException
	 */
	byte getByte() throws XDMException;
	
	/**
	 * 
	 * @return currencly selected item as {@literal double} primitive
	 * @throws XDMException
	 */
	double getDouble() throws XDMException;
	
	/**
	 * 
	 * @return currently selected item as {@literal float} primitive
	 * @throws XDMException
	 */
	float getFloat() throws XDMException;
	
	/**
	 * 
	 * @return currently selected item as {@literal int} primitive
	 * @throws XDMException
	 */
	int getInt() throws XDMException;
	
	/**
	 * 
	 * @return currently selected item as {@literal long} primitive
	 * @throws XDMException
	 */
	long getLong() throws XDMException;
	
	/**
	 * 
	 * @return currently selected item as XML Node
	 * @throws XDMException
	 */
	Node getNode() throws XDMException;
	
	/**
	 * 
	 * @return currently selected item's value
	 * @throws XDMException
	 */
	Object getObject() throws XDMException;
	
	/**
	 * 
	 * @return currently selected item as {@literal short} primitive
	 * @throws XDMException
	 */
	short getShort() throws XDMException;
	
	/**
	 * 
	 * @return currently selected item as String
	 * @throws XDMException
	 */
	String getString() throws XDMException;
	
	/**
	 * 
	 * @return the underlying list of selected items. Implemented in FixedCursor only.
	 * @throws XDMException
	 */
	List<Object> getList() throws XDMException;
	
	/**
	 * 
	 * @return currently selected item itself
	 * @throws XDMException
	 */
	XQItemAccessor getXQItem() throws XDMException;

	/**
	 * 
	 * @return true if cursor has a List of results fetched, false otherwise 
	 */
	boolean isFixed();
	
	/**
	 * Moves selected item one position next  
	 * 
	 * @return true is next item selected, false otherwise
	 * @throws XDMException
	 */
	boolean next() throws XDMException;
	
}
