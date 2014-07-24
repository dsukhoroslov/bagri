package com.bagri.xqj;

import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.xquery.XQConnection;
import javax.xml.xquery.XQException;
import javax.xml.xquery.XQExpression;
import javax.xml.xquery.XQItemAccessor;
import javax.xml.xquery.XQMetaData;
import javax.xml.xquery.XQPreparedExpression;
import javax.xml.xquery.XQStaticContext;

import com.bagri.xquery.api.XQProcessor;

public class BagriXQConnection extends BagriXQDataFactory implements XQConnection {
	
	private boolean autoCommit;
	private BagriXQMetaData metaData;
	private BagriXQStaticContext context;
	private XQProcessor processor;
	
	public BagriXQConnection(String address, int timeout) {

		metaData = new BagriXQMetaData(this, null);
		context = new BagriXQStaticContext();
	}

	public BagriXQConnection(String address, int timeout, String username, String password) {

		metaData = new BagriXQMetaData(this, username);
	}
	
	public XQProcessor getProcessor() {
		return this.processor;
	}
	
	public void setProcessor(XQProcessor processor) {
		this.processor = processor;
		BagriXQUtils.setXQProcessor(processor);
        logger.debug("setProcessor; got processor: {}", processor);
	}
	
	@Override
	public void close() throws XQException {
		
		//if (isClosed()) {
		//	throw new XQException("Connection is already closed");
		//}
		
        logger.trace("close");
		
		//client.getLifecycleService().kill();
		closed = true;
	}

	@Override
	public void commit() throws XQException {
		
		//client.getTransaction().commit();
	}

	@Override
	public XQExpression createExpression() throws XQException {
		
		if (isClosed()) {
			throw new XQException("Connection is closed");
		}
		return new BagriXQExpression(this);
	}

	@Override
	public XQExpression createExpression(XQStaticContext context) throws XQException {
		
		if (isClosed()) {
			throw new XQException("Connection is closed");
		}
		if (context == null) {
			throw new XQException("Context is null");
		}
		return new BagriXQExpression(this, context);
	}

	@Override
	public boolean getAutoCommit() throws XQException {
		
		return autoCommit;
	}

	@Override
	public XQMetaData getMetaData() throws XQException {
		
		if (isClosed()) {
			throw new XQException("Connection is closed");
		}
		return metaData;
	}

	@Override
	public XQStaticContext getStaticContext() throws XQException {
		
		if (isClosed()) {
			throw new XQException("Connection is closed");
		}
		return context; //new BagriXQStaticContext(context);
	}

	@Override
	public boolean isClosed() {
		
		return /*!client.getLifecycleService().isRunning() ||*/ closed;
	}

	@Override
	public XQPreparedExpression prepareExpression(String xquery) throws XQException {
		
		if (isClosed()) {
			throw new XQException("Connection is closed");
		}
		if (xquery == null) {
			throw new XQException("Provided xquery is null");
		}
		
		BagriXQPreparedExpression exp = new BagriXQPreparedExpression(this);
		exp.setXQuery(xquery);
		prepareQuery(exp);
		return exp;
	}

	@Override
	public XQPreparedExpression prepareExpression(Reader xquery) throws XQException {

		if (isClosed()) {
			throw new XQException("Connection is closed");
		}
		if (xquery == null) {
			throw new XQException("Provided xquery reader is null");
		}
		
		BagriXQPreparedExpression exp = new BagriXQPreparedExpression(this);
		String query = BagriXQUtils.textToString(xquery);
		exp.setXQuery(query);
		prepareQuery(exp);
		return exp;
	}

	@Override
	public XQPreparedExpression prepareExpression(InputStream xquery) throws XQException {

		if (isClosed()) {
			throw new XQException("Connection is closed");
		}
		if (xquery == null) {
			throw new XQException("Provided xquery stream is null");
		}

		BagriXQPreparedExpression exp = new BagriXQPreparedExpression(this);
		String query = BagriXQUtils.textToString(xquery);
		exp.setXQuery(query);
		prepareQuery(exp);
		return exp;
	}

	@Override
	public XQPreparedExpression prepareExpression(String xquery, XQStaticContext context) throws XQException {

		if (isClosed()) {
			throw new XQException("Connection is closed");
		}
		if (context == null) {
			throw new XQException("Context is null");
		}
		if (xquery == null) {
			throw new XQException("Provided xquery is null");
		}

		BagriXQPreparedExpression exp = new BagriXQPreparedExpression(this, context);
		exp.setXQuery(xquery);
		prepareQuery(exp, context);
		return exp;
	}

	@Override
	public XQPreparedExpression prepareExpression(Reader xquery, XQStaticContext context) throws XQException {

		if (isClosed()) {
			throw new XQException("Connection is closed");
		}
		if (context == null) {
			throw new XQException("Context is null");
		}
		if (xquery == null) {
			throw new XQException("Provided xquery reader is null");
		}

		BagriXQPreparedExpression exp = new BagriXQPreparedExpression(this, context);
		String query = BagriXQUtils.textToString(xquery);
		exp.setXQuery(query);
		prepareQuery(exp, context);
		return exp;
	}

	@Override
	public XQPreparedExpression prepareExpression(InputStream xquery, XQStaticContext context) throws XQException {

		if (isClosed()) {
			throw new XQException("Connection is closed");
		}
		if (context == null) {
			throw new XQException("Context is null");
		}
		if (xquery == null) {
			throw new XQException("Provided xquery stream is null");
		}

		BagriXQPreparedExpression exp = new BagriXQPreparedExpression(this, context);
		String query = BagriXQUtils.textToString(xquery);
		exp.setXQuery(query);
		prepareQuery(exp, context);
		return exp;
	}

	@Override
	public void rollback() throws XQException {
		
		//client.getTransaction().rollback();
	}

	@Override
	public void setAutoCommit(boolean autoCommit) throws XQException {
		
		this.autoCommit = autoCommit;
		//client.getTransaction().
	}

	@Override
	public void setStaticContext(XQStaticContext context) throws XQException {
		
		if (isClosed()) {
			throw new XQException("Connection is closed");
		}
		if (context == null) {
			throw new XQException("Context is null");
		}
		if (this.context != context) {
			this.context.copyFrom(context);
		}
	}

	public Iterator getResultIterator() {
		//List list = getResultList(); //new ArrayList();
		//XQSequence result = new IterableXQSequence(list.iterator()); // ScrollableXQSequence(list);
		//return new BagriXQSequenceIterator(result);
		return result;
	}
	
	private Iterator result;

	public List getResultList() {
		List list = new ArrayList();
		if (result != null) {
			while (result.hasNext()) {
				list.add(result.next());
			}
		}
		return list; 
	}
	

	public void executeCommand(String cmd, Map<QName, XQItemAccessor> bindings) throws XQException {
		
		executeCommand(cmd, bindings, context); //
	}
	
	public void executeCommand(String cmd, Map<QName, XQItemAccessor> bindings, 
			XQStaticContext ctx) throws XQException {
		
		if (isClosed()) {
			throw new XQException("Connection is closed");
		}
		// run cmd..
		//logger.info("executeCommand. got command: {}", cmd);
		processor.executeXCommand(cmd, bindings, ctx); //
	}

	public Iterator executeQuery(String query) throws XQException {
		
		return executeQuery(query, context); //this.getStaticContext());
	}
	
	public Iterator executeQuery(String query, XQStaticContext ctx) throws XQException {
		
		if (isClosed()) {
			throw new XQException("Connection is closed");
		}
		// run cmd..
		//logger.info("executeQuery. got query: {}", query);
		result = processor.executeXQuery(query, ctx);
		return result;
	}
	
	public void prepareQuery(BagriXQPreparedExpression exp) throws XQException {
		
		prepareQuery(exp, context); // this.getStaticContext());
	}
	
	public void prepareQuery(BagriXQPreparedExpression exp, XQStaticContext ctx) throws XQException {
		
		if (isClosed()) {
			throw new XQException("Connection is closed");
		}
		Collection<QName> vars = processor.prepareXQuery(exp.getXQuery(), ctx);
		exp.setVarNames(vars);
	}
	
	void bindVariable(QName varName, Object var) throws XQException {
		processor.bindVariable(varName, var);
	}
	
	void unbindVariable(QName varName) throws XQException {
		processor.unbindVariable(varName);
	}
}
