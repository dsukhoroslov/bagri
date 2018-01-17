package com.bagri.test.tpox.workload;

import static com.bagri.core.Constants.*;
import static com.bagri.support.util.PropUtils.setProperty;
import static com.bagri.support.util.XQUtils.*;
import static com.bagri.support.util.PropUtils.propsFromString;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;

import javax.xml.namespace.QName;
import javax.xml.xquery.XQConnection;
import javax.xml.xquery.XQDataSource;
import javax.xml.xquery.XQDynamicContext;
import javax.xml.xquery.XQException;
import javax.xml.xquery.XQExpression;
import javax.xml.xquery.XQItemType;
import javax.xml.xquery.XQPreparedExpression;
import javax.xml.xquery.XQResultSequence;
import javax.xml.xquery.XQSequence;
import javax.xml.xquery.XQSequenceType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.core.system.Parameter;
import com.bagri.xqj.BagriXQDataFactory;

public class BagriXQJPlugin extends BagriTPoXPlugin {

	private static final Logger logger = LoggerFactory.getLogger(BagriXQJPlugin.class);

	private static XQDataSource xqds; 
	static {
		try {
			xqds = initDataSource();
		} catch (XQException ex) {
			logger.error("", ex);
			System.exit(1);
		}
	}
	
    private static final ThreadLocal<XQConnection> xqc = new ThreadLocal<XQConnection>() {
		
    	@Override
    	protected XQConnection initialValue() {
    		try {
	    		XQConnection xqc = xqds.getConnection();
	    		setProperty(((BagriXQDataFactory) xqc).getProcessor().getProperties(), pn_client_fetchSize, null); 
	    		setProperty(((BagriXQDataFactory) xqc).getProcessor().getProperties(), pn_client_submitTo, null); 
	    		setProperty(((BagriXQDataFactory) xqc).getProcessor().getProperties(), pn_client_ownerParam, null); 
	    		logger.info("initialValue.exit; XQC: {}", xqc);
	    		return xqc;
    		} catch (XQException ex) {
    			logger.error("", ex);
    			return null;
    		}
    	}
    };
    
    public BagriXQJPlugin() {
    	super();
    }
	
    protected XQConnection getConnection() {
    	return xqc.get(); 
    }
    
	@Override
	public void close() throws SQLException {
		XQConnection conn = getConnection();
		if (!conn.isClosed()) {
			logger.info("close; XQC: {}", conn);
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

	private void bindParams(Map<String, Parameter> params, XQDynamicContext xqe) throws XQException {
	    for (Map.Entry<String, Parameter> e: params.entrySet()) {
	    	Parameter param = e.getValue();
	    	//if ("properties".equals(param.getType())) {
	    		// create and bind sequence with properties
	    	//	Properties props;
			//	try {
			//		props = propsFromString(param.getName());
			//	} catch (IOException ex) {
			//		logger.warn("bindParams.error; " + ex, ex);
			//		continue;
			//	}

				//XQItemType type = getConnection().createAtomicType(baseType, typeName, null);
				//XQSequence seq = getConnection().createSequence((java.util.Iterator) null);
				//xqe.bindSequence(new QName(e.getKey()), seq);
			//	XQSequenceType type = getConnection().createSequenceType(getConnection().createItemType(), XQSequenceType.OCC_ZERO_OR_MORE);
						//getConnection().createSequenceType(
						//		getConnection().createAtomicType(XQItemType.XQBASETYPE_STRING), XQSequenceType.OCC_ZERO_OR_MORE), XQSequenceType.OCC_ZERO_OR_MORE);
				//xqe.bindObject(new QName(e.getKey()), props, type);
	    	//} else {
		    	QName typeName = new QName(xs_ns, param.getType(), xs_prefix);
				int baseType = getBaseTypeForTypeName(typeName);
				XQItemType type = getConnection().createAtomicType(baseType, typeName, null);
				//xqe.bindAtomicValue(new QName(e.getKey()), param.getName(), type);
				xqe.bindObject(new QName(e.getKey()), getAtomicValue(baseType, param.getName()), type);
	    	//}
	    }
	}
	
	@Override
	protected int execCommand(String query, Map<String, Parameter> params) throws XQException {
		
		XQExpression xqe = getConnection().createExpression();
		bindParams(params, xqe);
	    xqe.executeCommand(query);
	    // do next somehow!
	    xqe.close();
		return 1;
	}
	
	@Override
	protected int execQuery(String query, Map<String, Parameter> params) throws XQException {

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
