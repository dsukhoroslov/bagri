package com.bagri.rest.service;

import java.util.Map;
import java.util.Properties;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class QueryParams {
	
	public String query;
	public Map<String, Object> params;
	public Properties props;

	public QueryParams() {
		// de-ser
	}
	
	public QueryParams(String query, Map<String, Object> params, Properties props) {
		this.query = query;
		this.params = params;
		this.props = props;
	}
	
	@Override
	public String toString() {
		return "QueryParams [query=" + query + "; params=" + params + "; props=" + props + "]"; 
	}


}
