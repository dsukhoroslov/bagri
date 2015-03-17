package com.bagri.xqj;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.xquery.XQConnection;
import javax.xml.xquery.XQException;
import javax.xml.xquery.XQExpression;
import javax.xml.xquery.XQItemAccessor;
import javax.xml.xquery.XQMetaData;
import javax.xml.xquery.XQPreparedExpression;
import javax.xml.xquery.XQStaticContext;

import com.bagri.common.util.XMLUtils;
import com.bagri.xdm.api.XDMTransactionManagement;

import static com.bagri.xdm.api.XDMTransactionManagement.TX_NO;
import static com.bagri.xqj.BagriXQConstants.ex_null_context;

public class BagriXQConnection extends BagriXQDataFactory implements XQConnection {
	
	private long txId;
	private boolean autoCommit;
	private boolean transactional;
	private BagriXQMetaData metaData;
	private BagriXQStaticContext context;
	
	public BagriXQConnection(String address) {

		metaData = new BagriXQMetaData(this, null);
		context = new BagriXQStaticContext();
		this.transactional = false;
	}

	public BagriXQConnection(String address, boolean transactional) {

		metaData = new BagriXQMetaData(this, null);
		context = new BagriXQStaticContext();
		this.transactional = transactional;
	}

	public BagriXQConnection(String address, String username, String password) {

		metaData = new BagriXQMetaData(this, username);
		context = new BagriXQStaticContext();
		this.transactional = false;
	}
	
	public BagriXQConnection(String address, String username, String password, boolean transactional) {

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
				getTxManager().commitTransaction(txId);
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

		if (transactional) {
			if (autoCommit) {
		        logger.info("commit; The connection is in AutoCommit state, nothing to commit explicitly.");
		        return;
			}
			getTxManager().commitTransaction(txId);
			txId = TX_NO;
		}
	}
	
	@Override
	public boolean getAutoCommit() throws XQException {
		
		return autoCommit;
	}

	private XDMTransactionManagement getTxManager() {
		return getProcessor().getRepository().getTxManagement();
	}

	@Override
	public void rollback() throws XQException {
		
		if (transactional) {
			if (autoCommit) {
		        logger.info("rollback; The connection is in AutoCommit state, nothing to rollback explicitly.");
		        return;
			}
			getTxManager().rollbackTransaction(txId);
			txId = TX_NO;
		}
	}

	@Override
	public void setAutoCommit(boolean autoCommit) throws XQException {

		if (this.autoCommit == autoCommit) {
			// no-op
			return;
		}
		if (transactional) {
			if (!this.autoCommit) {
				getTxManager().commitTransaction(txId);
				txId = TX_NO;
			}
			this.autoCommit = autoCommit;
		}
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
	
	public void executeCommand(String cmd, Map<QName, XQItemAccessor> bindings, 
			XQStaticContext ctx) throws XQException {
		
		checkConnection();
		if (transactional) {
			if (autoCommit || txId == TX_NO) {
				txId = getTxManager().beginTransaction();
			}
			try {
				getProcessor().executeXCommand(cmd, bindings, ctx);
				if (autoCommit && txId != TX_NO) {
					getTxManager().commitTransaction(txId);
					txId = TX_NO;
				}
			} catch (Throwable ex) {
				if (txId != TX_NO) {
					getTxManager().rollbackTransaction(txId);
					txId = TX_NO;
				}
				throw ex;
			}
		} else {
			getProcessor().executeXCommand(cmd, bindings, ctx);
		}
	}

	public Iterator executeQuery(String query) throws XQException {
		
		return executeQuery(query, context); //this.getStaticContext());
	}
	
	public Iterator executeQuery(String query, XQStaticContext ctx) throws XQException {
		
		checkConnection();
		Iterator result = null;
		if (transactional) {
			if (autoCommit || txId == TX_NO) {
				txId = getTxManager().beginTransaction();
			}
			try {
				result = getProcessor().executeXQuery(query, ctx);
				if (autoCommit && txId != TX_NO) {
					getTxManager().commitTransaction(txId);
					txId = TX_NO;
				}
			} catch (Throwable ex) {
				if (txId != TX_NO) {
					getTxManager().rollbackTransaction(txId);
					txId = TX_NO;
				}
				throw ex;
			}
		} else {
			result = getProcessor().executeXQuery(query, ctx);
		}
		
		if (result == null) {
			throw new XQException("got no response");
		}
		return result;
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
