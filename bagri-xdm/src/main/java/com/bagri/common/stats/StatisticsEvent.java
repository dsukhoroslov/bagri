package com.bagri.common.stats;

/**
 * Represents event happened in the system which affects collected statistics. 
 * 
 * @author Denis Sukhoroslov
 *
 */
public class StatisticsEvent {
	
	/**
	 * Events types
	 */
	public enum EventType {
		
		/**
		 * to delete existing statistics
		 */
		delete,
		
		/**
		 * to create or update statistics
		 */
		upsert;
		
	}
	
	private String series;
	private EventType type;
	private long timeStamp;
	private boolean success;
	private Object[] params;
	
	/**
	 * 
	 * @param series the statistics series name
	 * @param type the event type
	 */
	public StatisticsEvent(String series, EventType type) {
		this.series = series;
		this.type = type; 
		this.timeStamp = System.currentTimeMillis();
	}
	
	/**
	 * 
	 * @param name the statistics series name 
	 * @param success the flag indicating that event has happened as a result of correct system behavior {@code true} or not {@code false} 
	 * @param params the event payload
	 */
	public StatisticsEvent(String name, boolean success, Object[] params) {
		this(name, success, System.currentTimeMillis(), params);
	}

	/**
	 * 
	 * @param name the statistics series name 
	 * @param success the flag indicating that event has happened as a result of correct system behavior {@code true} or not {@code false} 
	 * @param timeStamp the date/time in milliseconds when the event has happened
	 * @param params the event payload
	 */
	public StatisticsEvent(String name, boolean success, long timeStamp, Object[] params) {
		this.series = name;
		this.success = success;
		this.timeStamp = timeStamp;
		this.type = EventType.upsert;
		this.params = params;
	}
	
	/**
	 * 
	 * @return the event payload
	 */
	public Object[] getParams() {
		return params;
	}
	
	/**
	 * 
	 * @param idx the index to get concrete payload value
	 * @return the payload value for the index provided
	 */
	public Object getParam(int idx) {
		if (params == null) {
			return null;
		}
		return params[idx];
	}
	
	/**
	 * 
	 * @return the the statistics series name
	 */
	public String getName() {
		return series;
	}
	
	/**
	 * 
	 * @return the flag indicating that event has happened as a result of correct system behavior {@code true} or not {@code false}
	 */
	public boolean isSuccess() {
		return success;
	}
	
	/**
	 * 
	 * @return the event type
	 */
	public EventType getType() {
		return type;
	}
	
	/**
	 * 
	 * @return the date/time in milliseconds when the event has happened
	 */
	public long getTimestamp() {
		return timeStamp;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "StatisticsEvent [name=" + series + ", type=" + type + 
				", stamp=" + new java.util.Date(timeStamp).toString() +
				", success=" + success + "]";
	}
	
}
