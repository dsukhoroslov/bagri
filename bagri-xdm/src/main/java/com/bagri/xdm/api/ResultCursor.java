package com.bagri.xdm.api;

import java.util.List;

import org.w3c.dom.Node;

public interface ResultCursor extends AutoCloseable {
	
	List<?> fetchAll();
	Object getAsIs() throws XDMException;
	
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
	boolean next() throws XDMException;
	
}
