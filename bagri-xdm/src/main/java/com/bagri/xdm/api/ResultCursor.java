package com.bagri.xdm.api;

import java.util.List;

import javax.xml.xquery.XQItemAccessor;

import org.w3c.dom.Node;

public interface ResultCursor extends AutoCloseable {
	
	boolean getBoolean() throws XDMException;
	byte getByte() throws XDMException;
	double getDouble() throws XDMException;
	float getFloat() throws XDMException;
	int getInt() throws XDMException;
	long getLong() throws XDMException;
	Node getNode() throws XDMException;
	Object getObject() throws XDMException;
	short getShort() throws XDMException;
	String getString() throws XDMException;
	
	List<?> getList() throws XDMException;
	XQItemAccessor getXQItem() throws XDMException;

	boolean getNext() throws XDMException;
	
}
