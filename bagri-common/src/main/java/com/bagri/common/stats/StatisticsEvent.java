package com.bagri.common.stats;

public class StatisticsEvent {
	
	public enum EventType {
		
		delete,
		upsert;
		
	}
	
	private String series;
	private EventType type;
	private long timeStamp;
	private boolean success;
	private Object[] params;
	
	public StatisticsEvent(String series, EventType type) {
		this.series = series;
		this.type = type; //EventType.delete;
		this.timeStamp = System.currentTimeMillis();
	}
	
	public StatisticsEvent(String name, boolean success, Object[] params) {
		this(name, success, System.currentTimeMillis(), params);
	}

	public StatisticsEvent(String name, boolean success, long timeStamp, Object[] params) {
		this.series = name;
		this.success = success;
		this.timeStamp = timeStamp;
		this.type = EventType.upsert;
		this.params = params;
	}
	
	public Object[] getParams() {
		return params;
	}
	
	public Object getParam(int idx) {
		if (params == null) {
			return null;
		}
		return params[idx];
	}
	
	public String getName() {
		return series;
	}
	
	public boolean isSuccess() {
		return success;
	}
	
	public EventType getType() {
		return type;
	}
	
	public long getTimestamp() {
		return timeStamp;
	}

	@Override
	public String toString() {
		return "StatisticsEvent [name=" + series + ", type=" + type + 
				", stamp=" + new java.util.Date(timeStamp).toString() +
				", success=" + success + "]";
	}
	
}
