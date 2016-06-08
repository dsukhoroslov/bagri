package com.bagri.xquery.api;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import javax.xml.namespace.QName;
import javax.xml.xquery.XQDataFactory;
import javax.xml.xquery.XQException;
import javax.xml.xquery.XQStaticContext;

import com.bagri.xdm.api.XDMRepository;

/**
 * abstracts (x-)query processing from the underlying XQuery engine implementation. Used as a link between Bagri XQJ implementation and low-level XDM API.   
 * 
 * @author Denis Sukhoroslov
 *
 */
public interface XQProcessor {
	
    /**
     * 
     * @return XDM repository
     */
    XDMRepository getRepository();
    
    /**
     * 
     * @return assigned XQJ data factory
     */
    XQDataFactory getXQDataFactory();
    
    /**
     * 
     * @param xRepo the XDM repository to assign with this XQ processor
     */
    void setRepository(XDMRepository xRepo);
    
    /**
     * 
     * @param xqFactory the XQJ data factory to assign with this XQ processor
     */
	void setXQDataFactory(XQDataFactory xqFactory);    

	/**
	 * 
	 * @return XQ processor properties
	 */
    Properties getProperties();
	
    /**
     * 
     * @param feature one of XQJ feature constants from {@code com.bagri.xdm.common.XDMConstant}
     * @return true if feature supported by the underlying XQuery implementation, false otherwise
     */
    boolean isFeatureSupported(int feature);
    
    Collection<QName> prepareXQuery(String query, XQStaticContext ctx) throws XQException;
    Iterator<?> executeXQuery(String query, XQStaticContext ctx) throws XQException;
    Iterator<?> executeXQuery(String query, Properties props) throws XQException;
    void bindVariable(QName varName, Object var) throws XQException;
    void unbindVariable(QName varName) throws XQException;

	Iterator<?> executeXCommand(String command, Map<QName, Object> params, XQStaticContext ctx) throws XQException;
    Iterator<?> executeXCommand(String command, Map<QName, Object> params, Properties props) throws XQException;

    Iterator<?> getResults();
    void setResults(Iterator<?> itr);
    
    void cancelExecution() throws XQException;
    // Saxon specific conversion
    // TODO: move this out of the interface!
	String convertToString(Object item, Properties props) throws XQException;
	
}
