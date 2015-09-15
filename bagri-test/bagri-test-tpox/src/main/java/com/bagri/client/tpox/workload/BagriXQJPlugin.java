package com.bagri.client.tpox.workload;

import static com.bagri.common.util.PropUtils.setProperty;
import static com.bagri.xdm.common.XDMConstants.*;
import static com.bagri.xqj.BagriXQUtils.*;
import static com.bagri.xqj.BagriXQDataSource.*;

import java.sql.SQLException;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.xquery.XQConnection;
import javax.xml.xquery.XQDataSource;
import javax.xml.xquery.XQDynamicContext;
import javax.xml.xquery.XQException;
import javax.xml.xquery.XQExpression;
import javax.xml.xquery.XQItemType;
import javax.xml.xquery.XQPreparedExpression;
import javax.xml.xquery.XQResultSequence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.xdm.system.XDMParameter;
import com.bagri.xqj.BagriXQDataSource;
import com.bagri.xqj.BagriXQDataFactory;

public class BagriXQJPlugin extends BagriTPoXPlugin {

	private static final Logger logger = LoggerFactory.getLogger(BagriXQJPlugin.class);

	private static final XQDataSource xqds; 
	
	static {
		xqds = new BagriXQDataSource();
		try {
		    xqds.setProperty(ADDRESS, System.getProperty(pn_schema_address));
		    xqds.setProperty(SCHEMA, System.getProperty(pn_schema_name));
		    xqds.setProperty(USER, System.getProperty(pn_schema_user));
		    xqds.setProperty(PASSWORD, System.getProperty(pn_schema_password));
		    xqds.setProperty(XQ_PROCESSOR, "com.bagri.xquery.saxon.XQProcessorClient");
		    xqds.setProperty(XDM_REPOSITORY, "com.bagri.xdm.client.hazelcast.impl.RepositoryImpl");
		    xqds.setProperty(pn_client_loginTimeout, System.getProperty(pn_client_loginTimeout));
		    xqds.setProperty(pn_client_bufferSize, System.getProperty(pn_client_bufferSize));
		    xqds.setProperty(pn_client_connectAttempts, System.getProperty(pn_client_connectAttempts));
		} catch (XQException ex) {
			logger.error("", ex);
		}
	}
	
    private static final ThreadLocal<XQConnection> xqc = new ThreadLocal<XQConnection>() {
		
    	@Override
    	protected XQConnection initialValue() {
    		try {
	    		XQConnection xqc = xqds.getConnection();
	    		setProperty(((BagriXQDataFactory) xqc).getProcessor().getProperties(), pn_client_fetchSize, null); 
	    		setProperty(((BagriXQDataFactory) xqc).getProcessor().getProperties(), pn_client_submitTo, null); 
	    		logger.info("initialValue.exit; XQC: {}", xqc);
	    		return xqc;
    		} catch (XQException ex) {
    			logger.error("", ex);
    			return null;
    		}
    	}
    };
    
    protected XQConnection getConnection() {
    	return xqc.get(); 
    }
    
    public BagriXQJPlugin() {
    	super();
    }
	
	@Override
	public void close() throws SQLException {
		XQConnection conn = getConnection();
		if (!conn.isClosed()) {
			logger.info("close; XQC: {}; hit count: {}; miss count: {}; overfetch count: {}", conn, cntHit, cntMiss, cntOvf);
			try {
				conn.close();
			} catch (XQException ex) {
				logger.error("close.error; " + ex, ex);
				throw new SQLException(ex);
			}
		} else {
			logger.debug("close; XQC is already closed: {}", conn);
		}
	}

	private void bindParams(Map<String, XDMParameter> params, XQDynamicContext xqe) throws XQException {
	    for (Map.Entry<String, XDMParameter> e: params.entrySet()) {
	    	XDMParameter param = e.getValue();
	    	QName typeName = new QName(xs_ns, param.getType(), xs_prefix);
			int baseType = getBaseTypeForTypeName(typeName);
			XQItemType type = getConnection().createAtomicType(baseType, typeName, null);
			//xqe.bindAtomicValue(new QName(e.getKey()), param.getName(), type);
			xqe.bindObject(new QName(e.getKey()), getAtomicValue(baseType, param.getName()), type);
	    }
	}
	
	@Override
	protected int execCommand(String query, Map<String, XDMParameter> params) throws XQException {
		
		XQExpression xqe = getConnection().createExpression();
		bindParams(params, xqe);
	    xqe.executeCommand(query);
	    // do next somehow!
	    xqe.close();
		return 1;
	}
	
	@Override
	protected int execQuery(String query, Map<String, XDMParameter> params) throws XQException {

	    XQPreparedExpression xqpe = getConnection().prepareExpression(query);
		bindParams(params, xqpe);
	    XQResultSequence xqs = xqpe.executeQuery();
	    int cnt = 0;
	    if (fetchSize > 0) {
	    	while (xqs.next() && cnt < fetchSize) {
	    		cnt++;
	    	}
	    } else {
	    	while (xqs.next()) {
	    		cnt++;
	    	}
	    }
	    xqs.close();
	    xqpe.close();
	    return cnt;
	}

	@Override
	protected Logger getLogger() {
		return logger;
	}
	
}
