package com.bagri.client.tpox.workload;

import static com.bagri.xdm.common.XDMConstants.xs_ns;
import static com.bagri.xdm.common.XDMConstants.xs_prefix;
import static com.bagri.xqj.BagriXQUtils.*;

import java.net.URI;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import javax.xml.namespace.QName;
import javax.xml.xquery.XQConnection;
import javax.xml.xquery.XQDynamicContext;
import javax.xml.xquery.XQException;
import javax.xml.xquery.XQExpression;
import javax.xml.xquery.XQItemType;
import javax.xml.xquery.XQPreparedExpression;
import javax.xml.xquery.XQResultSequence;

import net.sf.tpox.workload.parameter.ActualParamInfo;
import net.sf.tpox.workload.parameter.Parameter;
import net.sf.tpox.workload.transaction.Transaction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.bagri.xdm.system.XDMParameter;

public class BagriXQJPlugin extends BagriTPoXPlugin {

	private static final Logger logger = LoggerFactory.getLogger(BagriXQJPlugin.class);
	
    private static final ThreadLocal<XQConnection> xqc = new ThreadLocal<XQConnection>() {
		
    	@Override
    	protected XQConnection initialValue() {
    		//synchronized (context) {
    		ApplicationContext context = new ClassPathXmlApplicationContext(config);
    		XQConnection xqc = context.getBean("xqConnection", XQConnection.class);
    		logger.info("initialValue.exit; XQC: {}", xqc);
    		return xqc;
    		//}
    	}
    };
	
    protected XQConnection getConnection() {
    	return xqc.get(); 
    }
    
    public BagriXQJPlugin() {
    	super();
		//logger.trace("<init>. XQConnection: {}", xqc);
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

	@Override
	public int execute() throws SQLException {
		int transNo = wp.getNextTransNumToExecute(rand);
		Transaction tx = wp.getTransaction(transNo);
		int result = 0; 
		logger.trace("execute.enter; transaction: {}; #: {}; ", tx.getTransName(), transNo);

		Vector<Parameter>[] params = wp.getParameterMarkers();
		int size = (params[transNo].size() - 2)/3;
		
		ActualParamInfo param = wp.getParamMarkerActualValue(transNo, 0, rand);
		String query = param.getActualValue();
		param = wp.getParamMarkerActualValue(transNo, 1, rand);
		boolean isQuery = Boolean.parseBoolean(param.getActualValue());
		Map<String, XDMParameter> vars = new HashMap<>(size);
		String value;
		
		//logger.debug("execute; size: {}; rand: {}; transNo: {}", size, rand, transNo);
		try {
			for (int i=0; i < size; i++) {
				param = wp.getParamMarkerActualValue(transNo, i*3+2, rand);
				String name = param.getActualValue();
				param = wp.getParamMarkerActualValue(transNo, i*3+3, rand);
				String type = param.getActualValue();
				param = wp.getParamMarkerActualValue(transNo, i*3+4, rand);
				if (type.equals("document")) {
					value = new String(param.getDocument());
				} else {
					value = param.getActualValue();
				}
				vars.put(name, buildParam(type, value));
			}
			logger.trace("execute; query: {}; params: {}", query, vars);
		
			if (isQuery) {
				// use execQuery
				result = execQuery(query, vars);
			} else {
				// use execCommand
				result = execCommand(query, vars);
			}
		} catch (Throwable ex) {
			logger.error("execute.error", ex);
		}
		
		return result;
	}
	
	private void bindParams(Map<String, XDMParameter> params, XQDynamicContext xqe) throws XQException {
	    for (Map.Entry<String, XDMParameter> e: params.entrySet()) {
	    	XDMParameter param = e.getValue();
	    	QName typeName = new QName(xs_ns, param.getType(), xs_prefix);
			int baseType = getBaseTypeForTypeName(typeName);
			XQItemType type = getConnection().createAtomicType(baseType, typeName, null);
			//xqe.bindAtomicValue(new QName(e.getKey()), param.getName(), type);
			xqe.bindObject(new QName(e.getKey()), getAtomicValue(baseType, param.getName()), type);
	    	//if (e.getValue() instanceof Boolean) {
	    	//	xqe.bindBoolean(new QName(e.getKey()), (Boolean) e.getValue(), null);
	    	//} else if (e.getValue() instanceof Byte) {
		    //	xqe.bindByte(new QName(e.getKey()), (Byte) e.getValue(), null);
	    	//} else if (e.getValue() instanceof Double) {
		    //	xqe.bindDouble(new QName(e.getKey()), (Double) e.getValue(), null);
	    	//} else if (e.getValue() instanceof Float) {
		    //	xqe.bindFloat(new QName(e.getKey()), (Float) e.getValue(), null);
	    	//} else if (e.getValue() instanceof Integer) {
		    //	xqe.bindInt(new QName(e.getKey()), (Integer) e.getValue(), null);
	    	//} else if (e.getValue() instanceof Long) {
		    //	xqe.bindLong(new QName(e.getKey()), (Long) e.getValue(), null);
	    	//} else if (e.getValue() instanceof Short) {
		    //	xqe.bindShort(new QName(e.getKey()), (Short) e.getValue(), null);
	    	//} else {
	    	//	xqe.bindString(new QName(e.getKey()), e.getValue().toString(), null);
	    	//}
	    }
	}
	
	protected int execCommand(String query, Map<String, XDMParameter> params) throws XQException {
		
		XQExpression xqe = getConnection().createExpression();
		bindParams(params, xqe);
	    xqe.executeCommand(query);
	    // do next somehow!
		return 1;
	}
	
	protected int execQuery(String query, Map<String, XDMParameter> params) throws XQException {

		//logger.trace("execQuery; query: {}; params: {}", query, params);
		
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
	
}
