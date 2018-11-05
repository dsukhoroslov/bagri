package com.bagri.core.xquery.api;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.TimeZone;

import javax.xml.xquery.XQDataFactory;
import javax.xml.xquery.XQException;
import javax.xml.xquery.XQStaticContext;

import com.bagri.core.api.ResultCursor;
import com.bagri.core.api.SchemaRepository;
import com.bagri.core.model.Query;

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
    SchemaRepository getRepository();
    
    /**
     * 
     * @return assigned XQJ data factory
     */
    XQDataFactory getXQDataFactory();
    
    /**
     * 
     * @param xRepo the XDM repository to assign with this XQ processor
     */
    void setRepository(SchemaRepository xRepo);
    
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
     * @param feature one of XQJ feature constants from <code>com.bagri.core.Constants</code>
     * @return true if feature supported by the underlying XQuery implementation, false otherwise
     */
    boolean isFeatureSupported(int feature);
    
    /**
     * 
     * @param query the plain text query representation 
     * @param props Properties containing query processing instructions. Besides standard XQJ properties may contain additional XDM properties: ..
     * @return true if query is read-only, false otherwise
     * @throws XQException in case of query compilation error
     */
	boolean isQueryReadOnly(String query, Properties props) throws XQException;
    
    /**
     * parses and compiles XQuery provided. Returns back found variable names.   
     * 
     * @param query the plain text query representation
     * @param ctx the XQJ static context {@link XQStaticContext}
     * @return the list of bound variable names
     * @throws XQException in case of query preparation error
     */
    Collection<String> prepareXQuery(String query, XQStaticContext ctx) throws XQException;

    /**
     * 
     * @param query the plain text query representation
     * @param props Properties containing query processing instructions. Besides standard XQJ properties may contain additional XDM properties: ..
     * @return an Iterator over query results
     * @throws XQException in case of query processing error
     */
    Iterator<Object> executeXQuery(String query, Properties props) throws XQException;
    
    /**
     * executes XQuery provided. Query parameters are passed via params Map. 
     * Returns back {@link Iterator} over query results.
     * 
     * NOTE: implemented on the server side only
     * 
     * @param query the plain text query representation
     * @param props Properties containing query processing instructions. Besides standard XQJ properties may contain additional XDM properties: ..
     * @return an Iterator over query results
     * @throws XQException in case of query processing error
     */
    Iterator<Object> executeXQuery(String query, Map<String, Object> params, Properties props) throws XQException;

    /**
     * executes XQuery provided. If query contains variables it must be prepared via <code>prepareXQuery</code> and variables 
     * must be bound via <code>bindVariable</code> first. 
     * Returns back {@link ResultCursor} over query results.
     * 
     * NOTE: implemented on the client side only
     * 
     * @param query the plain text query representation
     * @param ctx the XQJ static context {@link XQStaticContext}
     * @return a cursor over query results
     * @throws XQException in case of query processing error
     */
    <T> ResultCursor<T> executeXQuery(String query, XQStaticContext ctx) throws XQException;
    
    /**
     * bounds variable name with value in internal XQuery processing context
     * 
     * @param varName the variable name
     * @param var the variable value
     * @throws XQException in case of binding error
     */
    void bindVariable(String varName, Object var) throws XQException;

    /**
     * removes binding for the variable name from internal XQuery processing context
     * 
     * @param varName the variable name
     * @throws XQException in case of unbinding error
     */
    void unbindVariable(String varName) throws XQException;

    /**
     * executes custom XQuery command. Currently supported commands are:
     * <ul>
     *   <li>get-document-content</li>
     *   <li>remove-document</li>
     *   <li>remove-collection-documents</li>
     *   <li>store-document</li>
     * </ul>
     * Command parameters are provided in <code>params</code> Map, if any.
     * Returns back {@link Iterator} over command execution results.
     * 
     * @param command the direct XDM command. Bypasses underlying XQuery engine and executed directly by low level XDM API.
     * @param params the map of command parameter name/value pairs, if any
     * @param ctx the XQJ static context {@link XQStaticContext}
     * @return an Iterator over command execution results
     * @throws XQException in case of command execution errors
     */
	Iterator<Object> executeXCommand(String command, Map<String, Object> params, XQStaticContext ctx) throws XQException;

    // TODO: the commands should not return results, probably..
	
    /**
     * executes custom XQuery command. Currently supported commands are:
     * <ul>
     *   <li>get-document-content</li>
     *   <li>remove-document</li>
     *   <li>remove-collection-documents</li>
     *   <li>store-document</li>
     * </ul>
     * Command parameters are provided in <code>params</code> Map, if any.
     * Returns back {@link Iterator} over command execution results.
     * 
     * @param command the direct XDM command. Bypasses underlying XQuery engine and executed directly by low level XDM API.
     * @param params the map of command parameter name/value pairs, if any
     * @param props Properties containing query processing instructions. Besides standard XQJ properties may contain additional XDM properties: ..
     * @return an Iterator over command execution results
     * @throws XQException in case of command execution errors
     */
    Iterator<Object> executeXCommand(String command, Map<String, Object> params, Properties props) throws XQException;

    /**
     * for internal use on server side only
     * 
     * @param query the plain text query representation
     * @return just executed query 
     * @throws XQException in case of query compilation errors
     */
    Query getCurrentQuery(final String query) throws XQException;

    /**
     * for internal use on server side only
     * 
     * @return cursor over cached results
     */
    //<T> ResultCursor<T> getResults();
     
    /**
     * for internal use on server side only
     * 
     * @param cursor the ResultCursor supporting results pagination
     */
    //<T> void setResults(ResultCursor<T> cursor);
    
    /**
     * cancels currently executing query or command
     * 
     * @throws XQException in case of any cancellation exception
     */
    void cancelExecution() throws XQException;

    // Saxon specific conversion
    // TODO: move this out of the interface..
    /**
     * converts query result into its string representation
     * 
     * @param item result item to be converted
     * @param props conversion properties
     * @return String item representation
     * @throws XQException in case of conversion errors
     */
	String convertToString(Object item, Properties props) throws XQException;
	
	/**
	 * set timeZone in the processor's dynamic context
	 * 
	 * @param timeZone the TimeZone to set
	 * @throws XQException in case of processing error
	 */
	void setTimeZone(TimeZone timeZone) throws XQException;
	
	/**
	 * clears local query cache
	 */
	void clearLocalCache();
	
	/**
	 * free resources allocated by the processor
	 */
	void close();
}
