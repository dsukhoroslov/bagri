package com.bagri.xqj;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.Callable;

import javax.xml.namespace.QName;
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
import com.bagri.xdm.domain.XDMDocument;

import static com.bagri.xdm.api.XDMTransactionManagement.TX_NO;
import static com.bagri.xdm.common.XDMConstants.ex_null_context;

public class BagriXQConnection extends BagriXQDataFactory implements XQConnection {
	
    private static final Logger logger = LoggerFactory.getLogger(BagriXQConnection.class);
	
	private long txId;
	private boolean autoCommit = true; // default value
	private boolean transactional = true; // default value
	private BagriXQMetaData metaData;
	private BagriXQStaticContext context;
	
	BagriXQConnection(String address) {

		metaData = new BagriXQMetaData(this, null);
		context = new BagriXQStaticContext();
		try {
			transactional = metaData.isTransactionSupported();
		} catch (XQException ex) {
			transactional = false;
		}
	}

	BagriXQConnection(String address, boolean transactional) {

		metaData = new BagriXQMetaData(this, null);
		context = new BagriXQStaticContext();
		this.transactional = transactional;
	}

	BagriXQConnection(String address, String username) {

		metaData = new BagriXQMetaData(this, username);
		context = new BagriXQStaticContext();
		try {
			transactional = metaData.isTransactionSupported();
		} catch (XQException ex) {
			transactional = false;
		}
	}
	
	BagriXQConnection(String address, String username, boolean transactional) {

		metaData = new BagriXQMetaData(this, username);
		context = new BagriXQStaticContext();
		this.transactional = transactional;
	}
	
	@Override
	public void close() throws XQException {
		
		//if (isClosed()) {
		//	throw new XQException("Connection is already closed");
		//}
		
		if (transactional) {
			if (autoCommit) {
				try {
					getTxManager().commitTransaction(txId);
				} catch (XDMException ex) {
					throw new XQException(ex.getMessage());
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
		
		checkConnection();
		return new BagriXQExpression(this);
	}

	@Override
	public XQExpression createExpression(XQStaticContext context) throws XQException {
		
		checkConnection();
		if (context == null) {
			throw new XQException(ex_null_context);
		}
		return new BagriXQExpression(this, context);
	}

	@Override
	public void commit() throws XQException {

		checkConnection();
		if (autoCommit) {
	        throw new XQException("The connection is in AutoCommit state, nothing to commit explicitly.");
		}

		if (transactional) {
			try {
				getTxManager().commitTransaction(txId);
			} catch (XDMException ex) {
				throw new XQException(ex.getMessage());
			}
			txId = TX_NO;
		}
	}
	
	@Override
	public boolean getAutoCommit() throws XQException {
		
		checkConnection();
		return autoCommit;
	}

	private XDMTransactionManagement getTxManager() {
		return getProcessor().getRepository().getTxManagement();
	}

	@Override
	public void rollback() throws XQException {
		
		checkConnection();
		if (autoCommit) {
	        throw new XQException("The connection is in AutoCommit state, nothing to rollback explicitly.");
		}

		if (transactional) {
			try {
				getTxManager().rollbackTransaction(txId);
			} catch (XDMException ex) {
				throw new XQException(ex.getMessage());
			}
			txId = TX_NO;
		}
	}

	@Override
	public void setAutoCommit(boolean autoCommit) throws XQException {

		checkConnection();
		if (this.autoCommit == autoCommit) {
			return;
		}
		if (transactional) {
			if (!this.autoCommit) {
				try {
					getTxManager().commitTransaction(txId);
				} catch (XDMException ex) {
					throw new XQException(ex.getMessage());
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
		
		checkConnection();
		return metaData;
	}

	@Override
	public XQStaticContext getStaticContext() throws XQException {
		
		checkConnection();
		return context; //new BagriXQStaticContext(context);
	}

	@Override
	public boolean isClosed() {
		
		return closed;
	}

	@Override
	public XQPreparedExpression prepareExpression(String xquery) throws XQException {
		
		checkConnection();
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

		checkConnection();
		if (xquery == null) {
			throw new XQException("Provided xquery reader is null");
		}
		
		BagriXQPreparedExpression exp = new BagriXQPreparedExpression(this);
		String query;
		try {
			query = XMLUtils.textToString(xquery);
		} catch (IOException ex) {
			throw new XQException(ex.getMessage());
		}
		exp.setXQuery(query);
		prepareQuery(exp);
		return exp;
	}

	@Override
	public XQPreparedExpression prepareExpression(InputStream xquery) throws XQException {

		checkConnection();
		if (xquery == null) {
			throw new XQException("Provided xquery stream is null");
		}

		BagriXQPreparedExpression exp = new BagriXQPreparedExpression(this);
		String query;
		try {
			query = XMLUtils.textToString(xquery);
		} catch (IOException ex) {
			throw new XQException(ex.getMessage());
		}
		exp.setXQuery(query);
		prepareQuery(exp);
		return exp;
	}

	@Override
	public XQPreparedExpression prepareExpression(String xquery, XQStaticContext context) throws XQException {

		checkConnection();
		if (context == null) {
			throw new XQException(ex_null_context);
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

		checkConnection();
		if (context == null) {
			throw new XQException(ex_null_context);
		}
		if (xquery == null) {
			throw new XQException("Provided xquery reader is null");
		}

		BagriXQPreparedExpression exp = new BagriXQPreparedExpression(this, context);
		String query;
		try {
			query = XMLUtils.textToString(xquery);
		} catch (IOException ex) {
			throw new XQException(ex.getMessage());
		}
		exp.setXQuery(query);
		prepareQuery(exp, context);
		return exp;
	}

	@Override
	public XQPreparedExpression prepareExpression(InputStream xquery, XQStaticContext context) throws XQException {

		checkConnection();
		if (context == null) {
			throw new XQException(ex_null_context);
		}
		if (xquery == null) {
			throw new XQException("Provided xquery stream is null");
		}

		BagriXQPreparedExpression exp = new BagriXQPreparedExpression(this, context);
		String query;
		try {
			query = XMLUtils.textToString(xquery);
		} catch (IOException ex) {
			throw new XQException(ex.getMessage());
		}
		exp.setXQuery(query);
		prepareQuery(exp, context);
		return exp;
	}

	@Override
	public void setStaticContext(XQStaticContext context) throws XQException {
		
		checkConnection();
		if (context == null) {
			throw new XQException(ex_null_context);
		}
		if (this.context != context) {
			this.context.copyFrom(context);
		}
	}

	public void executeCommand(String cmd, Map<QName, XQItemAccessor> bindings) throws XQException {
		
		executeCommand(cmd, bindings, context); 
	}
	
	public void executeCommand(final String cmd, final Map<QName, XQItemAccessor> bindings, 
			final XQStaticContext ctx) throws XQException {
		
		checkConnection();
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
				throw new XQException(ex.getMessage());
			}
		} else {
			getProcessor().executeXCommand(cmd, bindings, ctx);
		}
	}

	public Iterator executeQuery(String query) throws XQException {
		
		return executeQuery(query, context); //this.getStaticContext());
	}
	
	public Iterator executeQuery(final String query, final XQStaticContext ctx) throws XQException {
		
		checkConnection();
		Iterator result = null;
		if (transactional) {
			try {
				executeInTransaction(new Callable<Iterator>() {
					@Override
			    	public Iterator call() throws XQException {
						return getProcessor().executeXQuery(query, ctx);
			    	}
				});
			} catch (XDMException ex) {
				throw new XQException(ex.getMessage());
			}
		} else {
			result = getProcessor().executeXQuery(query, ctx);
		}
		
		if (result == null) {
			throw new XQException("got no response");
		}
		return result;
	}
	
	private <V> V executeInTransaction(Callable<V> executor) throws XDMException {
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
			if (ex instanceof XDMException) {
				throw (XDMException) ex;
			}
			throw new XDMException(ex, XDMException.ecTransaction);
		}
	}
	
	public void prepareQuery(BagriXQPreparedExpression exp) throws XQException {
		
		prepareQuery(exp, context); // this.getStaticContext());
	}
	
	public void prepareQuery(BagriXQPreparedExpression exp, XQStaticContext ctx) throws XQException {
		
		checkConnection();
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
