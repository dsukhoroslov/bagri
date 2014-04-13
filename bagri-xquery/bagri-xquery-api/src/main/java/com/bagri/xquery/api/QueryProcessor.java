package com.bagri.xquery.api;

import java.util.Collection;
import java.util.Iterator;

import javax.xml.namespace.QName;
import javax.xml.xquery.XQDataFactory;
import javax.xml.xquery.XQException;
import javax.xml.xquery.XQStaticContext;

import com.bagri.xdm.access.api.XDMDocumentManagement;

public interface QueryProcessor {
	
    XDMDocumentManagement getXdmManager();
    void setXdmManager(XDMDocumentManagement mgr);
    void setXQDataFactory(XQDataFactory xqFactory);    
	
    Collection<QName> prepareXQuery(String query, XQStaticContext ctx) throws XQException;
    Iterator evaluateXQuery(String query, XQStaticContext ctx) throws XQException;
    void bindVariable(QName varName, Object var) throws XQException;
    void unbindVariable(QName varName) throws XQException;
	
}
