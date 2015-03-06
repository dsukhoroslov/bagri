package com.bagri.common.stats;

public class StatisticsEvent {
	
	public enum EventType {
		
		delete,
		use,
		invoke;
		
	}
	
	private String name;
	private EventType type;
	private boolean success;
	private long duration;
	private long timeStamp;
	
	public StatisticsEvent(String name, EventType type) {
		this.name = name;
		this.timeStamp = System.currentTimeMillis();
		this.type = type; //EventType.delete;
	}
	
	public StatisticsEvent(String name, boolean success, long duration) {
		this(name, success, duration, System.currentTimeMillis());
	}

	public StatisticsEvent(String name, boolean success, long duration, long timeStamp) {
		this.name = name;
		this.success = success;
		this.duration = duration;
		this.timeStamp = timeStamp;
		this.type = EventType.invoke;
	}
	
	public StatisticsEvent(String name, boolean success, int count) {
		this(name, success, count, System.currentTimeMillis());
	}

	public StatisticsEvent(String name, boolean success, int count, long timeStamp) {
		this.name = name;
		this.success = success;
		this.duration = count;
		this.timeStamp = timeStamp;
		this.type = EventType.use;
	}
	
	public String getName() {
		return name;
	}
	
	public boolean isSuccess() {
		return success;
	}
	
	public long getDuration() {
		if (type == EventType.invoke) {
			return duration;
		}
		return 0;
	}
	
	public int getCount() {
		if (type == EventType.use) {
			return (int) duration;
		}
		return 0;
	}
	
	public EventType getType() {
		return type;
	}
	
	public long getTimestamp() {
		return timeStamp;
	}

	@Override
	public String toString() {
		return "StatisticsEvent [name=" + name + ", type=" + type + 
				", stamp=" + new java.util.Date(timeStamp).toString() +
				", success=" + success + ", duration=" + duration + "]";
	}
	
}
