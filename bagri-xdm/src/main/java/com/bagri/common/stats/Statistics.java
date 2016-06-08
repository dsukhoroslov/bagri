package com.bagri.common.stats;

import java.util.Map;

public abstract class Statistics {
	
	public static final String colon = ": ";
	public static final String semicolon = "; ";
	public static final String empty = ": 0; ";
	
	private String name;
	
	public Statistics(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public abstract String getDescription(); 
	public abstract String getHeader();
	public abstract Map<String, Object> toMap();
	public abstract void update(StatisticsEvent event);

}
