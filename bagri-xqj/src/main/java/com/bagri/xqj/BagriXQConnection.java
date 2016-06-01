package com.bagri.xqj;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.Callable;

import javax.xml.namespace.QName;
import javax.xml.xquery.XQCancelledException;
import javax.xml.xquery.XQConnection;
import javax.xml.xquery.XQException;
import javax.xml.xquery.XQExpression;
import javax.xml.xquery.XQItemAccessor;
import javax.xml.xquery.XQMetaData;
import javax.xml.xquery.XQPreparedExpression;
import javax.xml.xquery.XQStaticContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.common.util.XMLUtils;
import com.bagri.xdm.api.XDMException;
import com.bagri.xdm.api.XDMTransactionManagement;

import static com.bagri.xdm.api.XDMException.ecTransWrongState;
import static com.bagri.xdm.api.XDMTransactionManagement.TX_NO;
import static com.bagri.xqj.BagriXQErrors.ex_connection_closed;
import static com.bagri.xqj.BagriXQErrors.ex_null_context;
import static com.bagri.xquery.api.XQUtils.getXQException;


public class BagriXQConnection extends BagriXQDataFactory implements XQConnection {
	
    private static final Logger logger = LoggerFactory.getLogger(BagriXQConnection.class);
	
	private long txId;
	private boolean cancelled = false;
	private boolean autoCommit = true; // default value
	private boolean transactional = true; // default value
	private BagriXQMetaData metaData;
	private BagriXQStaticContext context;
	
	BagriXQConnection() {
		this(null, null);
	}

	BagriXQConnection(String username) {
		this(username, null);
	}
	
	BagriXQConnection(boolean transactional) {
		this(null, transactional);
	}

	BagriXQConnection(String username, Boolean transactional) {
		super();
		metaData = new BagriXQMetaData(this, username);
		context = new BagriXQStaticContext();
		if (transactional == null) {
			try {
				this.transactional = metaData.isTransactionSupported();
			} catch (XQException ex) {
				this.transactional = false;
			}
		} else {
			this.transactional = transactional;
		}
	}
	
	void cancel() throws XQException {

		//checkState();
		cancelled = true;
		// interrupt any current request.. 
		getProcessor().cancelExecution();
	}
	
	@Override
	public void close() throws XQException {
		
		//checkState();
		if (transactional) {
			if (autoCommit) {
				try {
					getTxManager().commitTransaction(txId);
				} catch (XDMException ex) {
		    		throw getXQException(ex);
				}
				txId = TX_NO;
			} else {
				// ??
			}
		}
		
		getProcessor().getRepository().close();
		closed = true;
		logger.debug("close.");
	}

	@Override
	public XQExpression createExpression() throws XQException {
		
		checkState(ex_connection_closed);
		return new BagriXQExpression(this);
	}

	@Override
	public XQExpression createExpression(XQStaticContext context) throws XQException {
		
		checkState(ex_connection_closed);
		if (context == null) {
			throw new XQException(ex_null_context);
		}
		return new BagriXQExpression(this, context);
	}

	@Override
	public void commit() throws XQException {

		checkState(ex_connection_closed);
		if (autoCommit) {
	        throw new XQException("The connection is in AutoCommit state, nothing to commit explicitly.", String.valueOf(ecTransWrongState));
		}

		if (transactional) {
			try {
				getTxManager().commitTransaction(txId);
			} catch (XDMException ex) {
	    		throw getXQException(ex);
			}
			txId = TX_NO;
		}
	}
	
	@Override
	public boolean getAutoCommit() throws XQException {
		
		checkState(ex_connection_closed);
		return autoCommit;
	}

	private XDMTransactionManagement getTxManager() {
		return getProcessor().getRepository().getTxManagement();
	}

	@Override
	public void rollback() throws XQException {
		
		checkState(ex_connection_closed);
		if (autoCommit) {
	        throw new XQException("The connection is in AutoCommit state, nothing to rollback explicitly.", String.valueOf(ecTransWrongState));
		}

		if (transactional) {
			try {
				getTxManager().rollbackTransaction(txId);
			} catch (XDMException ex) {
	    		throw getXQException(ex);
			}
			txId = TX_NO;
		}
	}

	@Override
	public void setAutoCommit(boolean autoCommit) throws XQException {

		checkState(ex_connection_closed);
		if (this.autoCommit == autoCommit) {
			return;
		}
		if (transactional) {
			if (!this.autoCommit) {
				try {
					getTxManager().commitTransaction(txId);
				} catch (XDMException ex) {
		    		throw getXQException(ex);
				}
				txId = TX_NO;
			}
		}
		this.autoCommit = autoCommit;
	}
	
	public boolean isTransactional() {
		return transactional;
	}
	
	public void setTransactional(boolean transactional) {
		// check current state ??
		this.transactional = transactional;
	}

	@Override
	public XQMetaData getMetaData() throws XQException {
		
		checkState(ex_connection_closed);
		return metaData;
	}

	@Override
	public XQStaticContext getStaticContext() throws XQException {
		
		checkState(ex_connection_closed);
		return context; 
	}

	@Override
	public boolean isClosed() {
		
		return closed;
	}

	@Override
	public XQPreparedExpression prepareExpression(String xquery) throws XQException {
		
		checkState(ex_connection_closed);
		if (xquery == null) {
			throw new XQException("Provided query is null");
		}
		
		BagriXQPreparedExpression exp = new BagriXQPreparedExpression(this);
		exp.setXQuery(xquery);
		prepareQuery(exp);
		return exp;
	}

	@Override
	public XQPreparedExpression prepareExpression(Reader xquery) throws XQException {

		String query = null;
		try {
			query = XMLUtils.textToString(xquery);
		} catch (IOException ex) {
    		throw getXQException(ex);
		}
		return prepareExpression(query);
	}

	@Override
	public XQPreparedExpression prepareExpression(InputStream xquery) throws XQException {

		String query = null;
		try {
			query = XMLUtils.textToString(xquery);
		} catch (IOException ex) {
    		throw getXQException(ex);
		}
		return prepareExpression(query);
	}

	@Override
	public XQPreparedExpression prepareExpression(String xquery, XQStaticContext context) throws XQException {

		checkState(ex_connection_closed);
		if (context == null) {
			throw new XQException(ex_null_context);
		}
		if (xquery == null) {
			throw new XQException("Provided query is null");
		}

		BagriXQPreparedExpression exp = new BagriXQPreparedExpression(this, context);
		exp.setXQuery(xquery);
		prepareQuery(exp, context);
		return exp;
	}

	@Override
	public XQPreparedExpression prepareExpression(Reader xquery, XQStaticContext context) throws XQException {

		String query = null;
		try {
			query = XMLUtils.textToString(xquery);
		} catch (IOException ex) {
    		throw getXQException(ex);
		}
		return prepareExpression(query, context);
	}

	@Override
	public XQPreparedExpression prepareExpression(InputStream xquery, XQStaticContext context) throws XQException {

		String query = null;
		try {
			query = XMLUtils.textToString(xquery);
		} catch (IOException ex) {
    		throw getXQException(ex);
		}
		return prepareExpression(query, context);
	}

	@Override
	public void setStaticContext(XQStaticContext context) throws XQException {
		
		checkState(ex_connection_closed);
		if (context == null) {
			throw new XQException(ex_null_context);
		}
		if (this.context != context) {
			this.context.copyFrom(context);
		}
	}

	public void executeCommand(String cmd, Map<QName, Object> bindings) throws XQException {
		
		executeCommand(cmd, bindings, context); 
	}
	
	public void executeCommand(final String cmd, final Map<QName, Object> bindings, 
			final XQStaticContext ctx) throws XQException {
		
		checkState(ex_connection_closed);
		cancelled = false;
		try {
			if (transactional) {
				try {
					executeInTransaction(new Callable<Void>() {
						@Override
				    	public Void call() throws XQException {
							getProcessor().executeXCommand(cmd, bindings, ctx);
							return null;
				    	}
					});
				} catch (XDMException ex) {
		    		throw getXQException(ex);
				}
			} else {
				getProcessor().executeXCommand(cmd, bindings, ctx);
			}
		} finally {
			if (cancelled) {
				throw new XQCancelledException("Command execution has been cancelled", null, null, -1, -1, -1, null, null, null);
			}
		}
	}

	@SuppressWarnings("rawtypes")
	public Iterator executeQuery(String query) throws XQException {
		
		return executeQuery(query, context); //this.getStaticContext());
	}
	
	@SuppressWarnings("rawtypes")
	public Iterator executeQuery(final String query, final XQStaticContext ctx) throws XQException {
		
		checkState(ex_connection_closed);
		Iterator result = null;
		cancelled = false;
		try {
			if (transactional) {
				try {
					executeInTransaction(new Callable<Iterator>() {
						@Override
				    	public Iterator call() throws XQException {
							return getProcessor().executeXQuery(query, ctx);
				    	}
					});
				} catch (XDMException ex) {
		    		throw getXQException(ex);
				}
			} else {
				result = getProcessor().executeXQuery(query, ctx);
			}
		} finally {
			if (cancelled) {
				throw new XQCancelledException("Query execution has been cancelled", null, null, -1, -1, -1, null, null, null);
			}
		}
		
		if (result == null) {
			throw new XQException("got no response");
		}
		return result;
	}
	
	private <V> V executeInTransaction(Callable<V> executor) throws XDMException, XQException {
		if (autoCommit || txId == TX_NO) {
			txId = getTxManager().beginTransaction();
		}
		try {
			V result = executor.call(); 
			if (autoCommit && txId != TX_NO) {
				getTxManager().commitTransaction(txId);
				txId = TX_NO;
			}
			return result;
		} catch (Throwable ex) {
			if (txId != TX_NO) {
				getTxManager().rollbackTransaction(txId);
				txId = TX_NO;
			}
			if (ex instanceof XQException) {
				throw (XQException) ex;
			}
			if (ex instanceof XDMException) {
				throw (XDMException) ex;
			}
			throw new XDMException(ex, XDMException.ecTransaction);
		}
	}
	
	private void prepareQuery(BagriXQPreparedExpression exp) throws XQException {
		
		prepareQuery(exp, context); // this.getStaticContext());
	}
	
	private void prepareQuery(BagriXQPreparedExpression exp, XQStaticContext ctx) throws XQException {
		
		checkState(ex_connection_closed);
		Collection<QName> vars = getProcessor().prepareXQuery(exp.getXQuery(), ctx);
		exp.setVarNames(vars);
	}
	
	void bindVariable(QName varName, Object var) throws XQException {
		getProcessor().bindVariable(varName, var);
	}
	
	void unbindVariable(QName varName) throws XQException {
		getProcessor().unbindVariable(varName);
	}
	
}
