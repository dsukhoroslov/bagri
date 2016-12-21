package com.bagri.server.hazelcast.predicate;

import java.util.Map;
import java.util.Properties;

import com.hazelcast.query.Predicate;

public class PropertyPredicate implements Predicate<String, Properties> {

	private String pName;
	private String pValue;
	
	public PropertyPredicate() {
		//
	}
	
	public PropertyPredicate(String pName, String pValue) {
		this.pName = pName;
		this.pValue = pValue;
	}

	@Override
	public boolean apply(Map.Entry<String, Properties> mapEntry) {
		Properties props = mapEntry.getValue();
		String prop = props.getProperty(pName);
		return prop != null && prop.equals(pValue); 
	}


}
