package com.bagri.support.stats;

import java.util.Map;

/**
 * A very basic abstract class to hold named statistics. 
 * 
 * @author Denis Sukhoroslov
 *
 */
public abstract class Statistics {
	
	public static final String colon = ": ";
	public static final String semicolon = "; ";
	public static final String empty = ": 0; ";
	
	private String name;
	
	/**
	 * 
	 * @param name the statistics name
	 */
	public Statistics(String name) {
		this.name = name;
	}
	
	/**
	 * 
	 * @return the statistics name
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * 
	 * @return the statistics description
	 */
	public abstract String getDescription();
	
	/**
	 * 
	 * @return the statistics header
	 */
	public abstract String getHeader();
	
	/**
	 * 
	 * @return the map of statistic keys and their values
	 */
	public abstract Map<String, Object> toMap();
	
	/**
	 * 
	 * @param event the event about statistics update
	 */
	public abstract void update(StatisticsEvent event);

}
