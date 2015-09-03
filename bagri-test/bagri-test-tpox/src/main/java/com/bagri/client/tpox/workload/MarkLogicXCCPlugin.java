package com.bagri.client.tpox.workload;

import com.bagri.xdm.system.XDMCardinality;
import com.bagri.xdm.system.XDMParameter;
import com.marklogic.xcc.Content;
import com.marklogic.xcc.ContentCreateOptions;
import com.marklogic.xcc.ContentFactory;
import com.marklogic.xcc.ContentSource;
import com.marklogic.xcc.Request;
import com.marklogic.xcc.ResultSequence;
import com.marklogic.xcc.Session;
import com.marklogic.xcc.ValueFactory;
import com.marklogic.xcc.exceptions.RequestException;
import com.marklogic.xcc.types.ValueType;
import com.marklogic.xcc.types.XName;
import com.marklogic.xcc.types.XdmValue;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;

import net.sf.tpox.workload.parameter.ActualParamInfo;
import net.sf.tpox.workload.parameter.Parameter;
import net.sf.tpox.workload.transaction.Transaction;
import net.xqj.marklogic.MarkLogicXQInsertOptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class MarkLogicXCCPlugin extends BagriTPoXPlugin {
	
    private static final transient Logger logger = LoggerFactory.getLogger(MarkLogicXCCPlugin.class);
	private static final AtomicInteger cnt = new AtomicInteger(0);
	
	private static final ThreadLocal<ContentSource> xcc = new ThreadLocal<ContentSource>() {
		
		@Override
		protected ContentSource initialValue() {
			//synchronized (context) {
			ApplicationContext context = new ClassPathXmlApplicationContext(config);
			ContentSource xcc = context.getBean("xccContentSource", ContentSource.class);
			logger.info("initialValue.exit; XCC: {}", xcc);
			return xcc;
			//}
 		}
		
	};
	
    protected ContentSource getContentSource() {
    	return xcc.get(); 
    }
    
    public MarkLogicXCCPlugin() {
    	super();
    	//logger.debug("<init>. Spring context: {}", config);
    }
	

	@Override
	public void close() throws SQLException {
		ContentSource xcs = getContentSource();
		logger.info("close; XCC: {}", xcs);
		xcs.getConnectionProvider().shutdown(null);
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
				vars.put(name, buildMLParam(type, value));
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
	
	protected Object buildMLParam(String type, String value) {
		//return new XDMParameter(value, type, XDMCardinality.one);
		
		if ("boolean".equals(type)) {
			return new Boolean(value);
		}
		if ("byte".equals(type)) {
			return new Byte(value);
		}
		if ("char".equals(type)) {
			return new Character(value.charAt(0));
		}
		if ("double".equals(type)) {
			return new Double(value);
		}
		if ("int".equals(type)) {
			return new Integer(value);
		}
		if ("float".equals(type)) {
			return new Float(value);
		}
		if ("long".equals(type)) {
			return new Long(value);
		}
		if ("short".equals(type)) {
			return new Short(value);
		}
		return value;
	}
	
	protected int execCommand(String command, Map<String, Object> params) throws RequestException {
        if ("insertDocument".equals(command)) {

        	ContentCreateOptions options = ContentCreateOptions.newXmlInstance();
        	//options.setNamespace("MyNameSpace");
            String collect = (String) params.get("collect");
            if (collect != null) {
            	options.setCollections(new String[] {collect});
            }
            String prefix = (String) params.get("prefix");
            if (prefix == null) {
            	prefix = "doc";
            }
            String uri = prefix + cnt.getAndIncrement() + ".xml";
            String doc = (String) params.get("doc");
        	Content content = ContentFactory.newContent(uri, doc, options);
    		ContentSource xcs = getContentSource();
    		Session xss = xcs.newSession();
        	xss.insertContent(content);        	
            return 1;
        }
		return 0;
	}
	
	private void bindParams(Map<String, Object> params, Request request) { //throws RequestException {
	    for (Map.Entry<String, Object> e: params.entrySet()) {
			XName name = new XName(e.getKey());
	    	XdmValue value;
	    	if (e.getValue() instanceof Boolean) {
	    		value = ValueFactory.newXSBoolean((Boolean) e.getValue());
	    	} else if (e.getValue() instanceof Byte) {
	    		value = ValueFactory.newXSInteger((Byte) e.getValue());
	    	} else if (e.getValue() instanceof Double) {
	    		value = ValueFactory.newValue(ValueType.XS_DOUBLE, (Double) e.getValue());
	    	} else if (e.getValue() instanceof Float) {
	    		value = ValueFactory.newValue(ValueType.XS_FLOAT, (Float) e.getValue());
	    	} else if (e.getValue() instanceof Integer) {
				value = ValueFactory.newXSInteger((Integer) e.getValue());
	    	} else if (e.getValue() instanceof Long) {
	    		value = ValueFactory.newXSInteger((Long) e.getValue());
	    	} else if (e.getValue() instanceof Short) {
	    		value = ValueFactory.newXSInteger((Short) e.getValue());
	    	} else {
				value = ValueFactory.newXSString(e.getValue().toString());
	    	}
			request.setVariable(ValueFactory.newVariable(name, value));
	    }
	}
	
	protected int execQuery(String query, Map<String, Object> params) throws RequestException {
		
		ContentSource xcs = getContentSource();
		Session xss = xcs.newSession();
		//xss.getDefaultRequestOptions().setResultBufferSize(fetchSize);
		Request request = xss.newAdhocQuery(query);
		bindParams(params, request);
		ResultSequence rs = xss.submitRequest(request);		
	    int cnt = 0;
	    if (fetchSize > 0) {
	    	while (rs.hasNext() && cnt < fetchSize) {
	    		rs.next();
	    		cnt++;
	    	}
	    } else {
	    	while (rs.hasNext()) {
	    		rs.next();
	    		cnt++;
	    	}
	    }
	    rs.close();
	    xss.close();
	    return cnt;
	}

}
