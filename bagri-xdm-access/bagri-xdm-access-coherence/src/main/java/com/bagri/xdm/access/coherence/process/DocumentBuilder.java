package com.bagri.xdm.access.coherence.process;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.util.aggregator.AbstractAggregator;

public class DocumentBuilder extends AbstractAggregator {
	
    protected final transient Logger logger = LoggerFactory.getLogger(getClass());
    
	protected int docType;
    protected String template;
    protected Map<String, String> params = new HashMap<String, String>();
    
    public DocumentBuilder() {
    	//
    }

    public DocumentBuilder(int docType, String template, Map params) {
    	this.docType = docType;
    	this.template = template;
    	if (params != null) {
    		this.params.putAll(params);
    	}
    }

    @Override
    public Object aggregate(Set entries) {
        //logger.trace("aggregate.enter; entries: {}", entries.size());
		//XDMDocumentManagerServer xdm = ((SpringAwareCacheFactory) CacheFactory.getConfigurableCacheFactory()).getSpringBean("xdmManager", XDMDocumentManagerServer.class);
		//if (xdm == null) {
		//	throw new IllegalStateException("XDM Server Context is not ready");
		//}
		return null; //xdm.buildDocument(docType, template, params, entries);
    }
    
	@Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public Object aggregateResults(Collection results) {
        logger.trace("aggregateResults.enter; results: {}", results.size());
        Collection<String> result = new ArrayList<String>();
        Iterator itr = results.iterator(); 
        for (Collection<String> all: (Collection<Collection<String>>) results) {
       		result.addAll(all);
        }
        logger.trace("aggregateResults.exit; returning: {}", result.size());
        return result;
    }

	@Override
    protected Object finalizeResult(boolean fFinal) {
        logger.trace("finalizeResult. final: {}", fFinal);
        return null;
	}

	@Override
    protected void init(boolean fFinal) {
        logger.trace("init.enter; final: {}", fFinal);
	}

	@Override
    protected void process(Object o, boolean fFinal) {
        logger.trace("process.enter; Object: {}; final: {}", o, fFinal);
	}

	@Override
	public void readExternal(PofReader in) throws IOException {
		docType = in.readInt(0);
		template = in.readString(1);
		in.readMap(2, params);
	}

	@Override
	public void writeExternal(PofWriter out) throws IOException {
		out.writeInt(0, docType);
		out.writeString(1, template);
		out.writeMap(2, params, String.class, String.class);
	}

	
}
