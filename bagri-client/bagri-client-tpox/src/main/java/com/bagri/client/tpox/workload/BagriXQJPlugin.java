package com.bagri.client.tpox.workload;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import javax.xml.namespace.QName;
import javax.xml.xquery.XQConnection;
import javax.xml.xquery.XQException;
import javax.xml.xquery.XQExpression;
//import javax.xml.xquery.XQItemType;
import javax.xml.xquery.XQPreparedExpression;
import javax.xml.xquery.XQResultSequence;

//import net.sf.tpox.workload.core.WorkloadProcessor;
import net.sf.tpox.workload.parameter.ActualParamInfo;
import net.sf.tpox.workload.parameter.Parameter;
import net.sf.tpox.workload.transaction.Transaction;
//import net.sf.tpox.workload.transaction.javaplugin.GenericJavaClassPlugin;
//import net.sf.tpox.workload.util.WorkloadEnvironment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class BagriXQJPlugin extends BagriTPoXPlugin {

    private static final transient Logger logger = LoggerFactory.getLogger(BagriXQJPlugin.class);

    private XQConnection xqc;
    
    public BagriXQJPlugin() {
    	String config = System.getProperty("xqj.spring.context");
    	logger.debug("<init>. Spring context: {}", config);
    	if (config != null) {
    	    ApplicationContext context = new ClassPathXmlApplicationContext(config);
    		xqc = context.getBean("xqConnection", XQConnection.class);
    	}
		logger.trace("<init>. XQConnection: {}", xqc);
    }
	
	@Override
	public void close() throws SQLException {
		try {
			xqc.close();
		} catch (XQException ex) {
			throw new SQLException(ex);
		}
		logger.trace("close");
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
		Map<String, Object> vars = new HashMap<String, Object>(size);
		String value;
		
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
			vars.put(name, value); //buildParam(type, value));
		}
		logger.trace("execute; query: {}; params: {}", query, vars);
		
		try {
			if (isQuery) {
				// use execQuery
				result = execQuery(query, vars);
			} else {
				// use execCommand
				result = execCommand(query, vars);
			}
		} catch (XQException ex) {
			throw new SQLException(ex);
		}
		
		return result;
	}
	
	private Object buildParam(String type, String value) {
		return value;
	}
    
	private int execCommand(String query, Map<String, Object> params) throws XQException {
		
		XQExpression xqe = xqc.createExpression();
	    for (Map.Entry<String, Object> e: params.entrySet()) {
		    xqe.bindString(new QName(e.getKey()), e.getValue().toString(), null);
	    }
	    xqe.executeCommand(query);
		return 1;
	}
	
	private int execQuery(String query, Map<String, Object> params) throws XQException {
		
	    XQPreparedExpression xqpe = xqc.prepareExpression(query);
	    for (Map.Entry<String, Object> e: params.entrySet()) {
		    xqpe.bindString(new QName(e.getKey()), e.getValue().toString(), null);
	    }
	    XQResultSequence xqs = xqpe.executeQuery();
	    if (xqs.next()) {
	    	return 1;
	    }
		return 0;
	}
	
}
