package com.bagri.test.tpox.workload;

import static com.bagri.xdm.common.XDMConstants.*;

import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.xdm.system.Parameter;
import com.marklogic.xcc.Content;
import com.marklogic.xcc.ContentCreateOptions;
import com.marklogic.xcc.ContentFactory;
import com.marklogic.xcc.ContentSource;
import com.marklogic.xcc.ContentSourceFactory;
import com.marklogic.xcc.Request;
import com.marklogic.xcc.ResultSequence;
import com.marklogic.xcc.Session;
import com.marklogic.xcc.ValueFactory;
import com.marklogic.xcc.exceptions.RequestException;
import com.marklogic.xcc.types.ValueType;
import com.marklogic.xcc.types.XName;
import com.marklogic.xcc.types.XdmValue;

public class MarkLogicXCCPlugin extends BagriTPoXPlugin {
	
    private static final transient Logger logger = LoggerFactory.getLogger(MarkLogicXCCPlugin.class);
	private static final AtomicInteger cnt = new AtomicInteger(0);
	
	private static final ThreadLocal<ContentSource> xcc = new ThreadLocal<ContentSource>() {
		
		@Override
		protected ContentSource initialValue() {
			ContentSource xcc = ContentSourceFactory.newContentSource(
					System.getProperty(pn_schema_host), 
					Integer.parseInt(System.getProperty(pn_schema_port)), 
					System.getProperty(pn_schema_user), 
					System.getProperty(pn_schema_password), 
					System.getProperty(pn_schema_name));
			logger.info("initialValue.exit; XCC: {}", xcc);
			return xcc;
 		}
		
	};
	
    protected ContentSource getContentSource() {
    	return xcc.get(); 
    }
    
    public MarkLogicXCCPlugin() {
    	super();
    }
	

	@Override
	public void close() throws SQLException {
		ContentSource xcs = getContentSource();
		logger.info("close; XCC: {}", xcs);
		xcs.getConnectionProvider().shutdown(null);
	}

	@Override
	protected int execCommand(String command, Map<String, Parameter> params) throws RequestException {
        if ("insertDocument".equals(command)) {

        	ContentCreateOptions options = ContentCreateOptions.newXmlInstance();
        	//options.setNamespace("MyNameSpace");
            String collect = params.get("collect").getName();
            if (collect != null) {
            	options.setCollections(new String[] {collect});
            }
            String prefix = params.get("prefix").getName();
            if (prefix == null) {
            	prefix = "doc";
            }
            String uri = prefix + cnt.getAndIncrement() + ".xml";
            String doc = params.get("doc").getName();
        	Content content = ContentFactory.newContent(uri, doc, options);
    		ContentSource xcs = getContentSource();
    		Session xss = xcs.newSession();
        	xss.insertContent(content);       
        	xss.close();
            return 1;
        }
		return 0;
	}
	
	private void bindParams(Map<String, Parameter> params, Request request) { //throws RequestException {
	    for (Map.Entry<String, Parameter> e: params.entrySet()) {
			XName name = new XName(e.getKey());
	    	XdmValue value;
	    	switch (e.getValue().getType()) {
	    		case "boolean": {
	    			value = ValueFactory.newXSBoolean(new Boolean(e.getValue().getName()));
	    			break;
	    		}
	    		case "byte": 
	    		case "int": 
	    		case "long": 
	    		case "short": {
	    			value = ValueFactory.newXSInteger(new Integer(e.getValue().getName()));
	    			break;
	    		}
	    		case "double": {
	    			value = ValueFactory.newValue(ValueType.XS_DOUBLE, new Double(e.getValue().getName()));
	    			break;
	    		}
	    		case "float": {
	    			value = ValueFactory.newValue(ValueType.XS_FLOAT, new Float(e.getValue().getName()));
	    			break;
	    		}
	    		default: 
	    			value = ValueFactory.newXSString(e.getValue().getName());
	    	}
			request.setVariable(ValueFactory.newVariable(name, value));
	    }
	}
	
	@Override
	protected int execQuery(String query, Map<String, Parameter> params) throws RequestException {
		
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

	@Override
	protected Logger getLogger() {
		return logger;
	}


}
