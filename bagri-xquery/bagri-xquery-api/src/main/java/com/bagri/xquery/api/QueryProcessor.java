package com.bagri.xquery.api;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import javax.xml.namespace.QName;
import javax.xml.xquery.XQDataFactory;
import javax.xml.xquery.XQException;
import javax.xml.xquery.XQStaticContext;

import com.bagri.xdm.access.api.XDMDocumentManagement;
import com.bagri.xdm.access.api.XDMQueryManagement;

public interface QueryProcessor {
	
    XDMDocumentManagement getXdmManager();
    void setXdmManager(XDMDocumentManagement mgr);
    void setXQDataFactory(XQDataFactory xqFactory);    

    XDMQueryManagement getXQManager();
    void setXQManager(XDMQueryManagement mgr);
    
    Properties getProperties();
	boolean isFeatureSupported(int feature);
    
    Collection<QName> prepareXQuery(String query, XQStaticContext ctx) throws XQException;
    Iterator executeXQuery(String query, XQStaticContext ctx) throws XQException;
    Iterator executeXQuery(String query, Properties props) throws XQException;
    void bindVariable(QName varName, Object var) throws XQException;
    void unbindVariable(QName varName) throws XQException;
	
}
