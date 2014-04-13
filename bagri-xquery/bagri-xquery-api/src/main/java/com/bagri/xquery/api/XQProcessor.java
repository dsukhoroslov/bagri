package com.bagri.xquery.api;

import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.xquery.XQException;
import javax.xml.xquery.XQItemAccessor;
import javax.xml.xquery.XQItemType;
import javax.xml.xquery.XQStaticContext;

public interface XQProcessor extends QueryProcessor {

	// todo: think about return type: Object vs Iterator..
    Object processCommand(String command, Map<QName, XQItemAccessor> bindings, XQStaticContext ctx) throws XQException;
	String convertToString(Object item) throws XQException;
	
}
