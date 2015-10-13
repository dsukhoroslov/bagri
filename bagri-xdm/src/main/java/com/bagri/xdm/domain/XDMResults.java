package com.bagri.xdm.domain;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class XDMResults {
	
	private Map<String, Object> params;
	private Collection<Long> docIds;
	private Collection<Object> results;
	
	public XDMResults() {
		//
	}
	
	public XDMResults(Map<String, Object> params, Collection<Long> docIds, Collection<Object> results) {
		//super();
		this.params = params;
		this.docIds = docIds;
		this.results = results;
	}

	public Map<String, Object> getParams() {
		return params;
	}

	public Collection<Long> getDocIds() {
		return docIds;
	}
	
	public Collection<Object> getResults() {
		return results;
	}
	

}
