package com.bagri.xqj;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import javax.xml.namespace.QName;
import javax.xml.xquery.XQCancelledException;
import javax.xml.xquery.XQConnection;
import javax.xml.xquery.XQException;
import javax.xml.xquery.XQExpression;
import javax.xml.xquery.XQMetaData;
import javax.xml.xquery.XQPreparedExpression;
import javax.xml.xquery.XQStaticContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.core.api.ResultCursor;
import com.bagri.core.api.TransactionManagement;
import com.bagri.core.api.BagriException;
import com.bagri.support.util.XMLUtils;

import static com.bagri.core.api.TransactionManagement.TX_NO;
import static com.bagri.core.api.BagriException.ecTransWrongState;
import static com.bagri.core.xquery.XQUtils.getXQException;
import static com.bagri.xqj.BagriXQErrors.ex_connection_closed;
import static com.bagri.xqj.BagriXQErrors.ex_null_context;


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
				} catch (BagriException ex) {
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
			} catch (BagriException ex) {
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

	private TransactionManagement getTxManager() {
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
			} catch (BagriException ex) {
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
				} catch (BagriException ex) {
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

	void executeCommand(String cmd, Map<QName, Object> bindings) throws XQException {
		
		executeCommand(cmd, bindings, context); 
	}
	
	void executeCommand(final String cmd, final Map<QName, Object> bindings, final XQStaticContext ctx) throws XQException {
		
		checkState(ex_connection_closed);
		cancelled = false;
		try {
			final Map<String, Object> params = new HashMap<>(bindings.size());
			for (Map.Entry<QName, Object> e: bindings.entrySet()) {
				params.put(toStringName(e.getKey()), e.getValue());
			}
			if (transactional) {
				try {
					executeInTransaction(new Callable<Void>() {
						@Override
				    	public Void call() throws XQException {
							getProcessor().executeXCommand(cmd, params, ctx);
							return null;
				    	}
					});
				} catch (BagriException ex) {
		    		throw getXQException(ex);
				}
			} else {
				getProcessor().executeXCommand(cmd, params, ctx);
			}
		} finally {
			if (cancelled) {
				throw new XQCancelledException("Command execution has been cancelled", null, null, -1, -1, -1, null, null, null);
			}
		}
	}

	ResultCursor executeQuery(String query) throws XQException {
		
		return executeQuery(query, context); 
	}
	
	ResultCursor executeQuery(final String query, final XQStaticContext ctx) throws XQException {
		
		checkState(ex_connection_closed);
		ResultCursor result = null;
		cancelled = false;
		try {
			if (transactional) {
				try {
					executeInTransaction(new Callable<ResultCursor>() {
						@Override
				    	public ResultCursor call() throws XQException {
							return getProcessor().executeXQuery(query, ctx);
				    	}
					});
				} catch (BagriException ex) {
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
	
	private <V> V executeInTransaction(Callable<V> executor) throws BagriException, XQException {
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
			if (ex instanceof BagriException) {
				throw (BagriException) ex;
			}
			throw new BagriException(ex, BagriException.ecTransaction);
		}
	}
	
	private void prepareQuery(BagriXQPreparedExpression exp) throws XQException {
		
		prepareQuery(exp, context); 
	}
	
	private void prepareQuery(BagriXQPreparedExpression exp, XQStaticContext ctx) throws XQException {
		
		checkState(ex_connection_closed);
		Collection<String> vars = getProcessor().prepareXQuery(exp.getXQuery(), ctx);
		Collection<QName> names = new ArrayList<>(vars.size());
		for (String vName: vars) {
			names.add(QName.valueOf(vName));
		}
		exp.setVarNames(names);
	}
	
	void bindVariable(QName varName, Object var) throws XQException {
		getProcessor().bindVariable(toStringName(varName), var);
	}
	
	void unbindVariable(QName varName) throws XQException {
		getProcessor().unbindVariable(toStringName(varName));
	}
	
	private String toStringName(QName qName) {
		return qName.toString();
	}
	
}
