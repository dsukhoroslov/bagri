package com.bagri.xdm.domain;

import java.util.List;
import java.util.Map;

public class XDMResults {
	
	private Map<String, Object> params;
	private List<Long> docIds;
	private List<Object> results;
	
	public XDMResults() {
		//
	}
	
	public XDMResults(Map<String, Object> params, List<Long> docIds, List<Object> results) {
		//super();
		this.params = params;
		this.docIds = docIds;
		this.results = results;
	}

	public Map<String, Object> getParams() {
		return params;
	}

	public List<Long> getDocIds() {
		return docIds;
	}
	
	public List<Object> getResults() {
		return results;
	}
	

}
