package com.bagri.xdm.domain;

import java.util.Collection;
import java.util.Map;

import javax.xml.namespace.QName;

public class XDMResults {
	
	private Map<QName, Object> params;
	private Collection<Long> docIds;
	private Collection<Object> results;
	
	public XDMResults() {
		//
	}
	
	public XDMResults(Map<QName, Object> params, Collection<Long> docIds, Collection<Object> results) {
		//super();
		this.params = params;
		this.docIds = docIds;
		this.results = results;
	}

	public Map<QName, Object> getParams() {
		return params;
	}

	public Collection<Long> getDocIds() {
		return docIds;
	}
	
	public Collection<Object> getResults() {
		return results;
	}
	

}
