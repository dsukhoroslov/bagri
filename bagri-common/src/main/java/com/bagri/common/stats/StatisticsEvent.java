package com.bagri.common.stats;

public class StatisticsEvent {
	
	public enum EventType {
		
		delete,
		use,
		invoke,
		index;
		
	}
	
	private String name;
	private EventType type;
	private boolean success;
	private long duration;
	private long timeStamp;
	private boolean unique;
	
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

	public StatisticsEvent(String name, boolean add, int count, int size, boolean unique) {
		this.name = name;
		this.success = add;
		this.duration = count;
		this.timeStamp = size;
		this.unique = unique;
		this.type = EventType.index;
	}
	
	public String getName() {
		return name;
	}
	
	public boolean isSuccess() {
		return success;
	}
	
	public boolean isUnique() {
		return unique;
	}
	
	public long getDuration() {
		if (type == EventType.invoke) {
			return duration;
		}
		return 0;
	}
	
	public int getCount() {
		if (type == EventType.use || type == EventType.index) {
			return (int) duration;
		}
		return 0;
	}
	
	public EventType getType() {
		return type;
	}
	
	public long getTimestamp() {
		if (type == EventType.invoke || type == EventType.use) {
			return timeStamp;
		}
		return 0;
	}

	public long getSize() {
		if (type == EventType.index) {
			return timeStamp;
		}
		return 0;
	}
	
	@Override
	public String toString() {
		return "StatisticsEvent [name=" + name + ", type=" + type + 
				", stamp=" + new java.util.Date(timeStamp).toString() +
				", success=" + success + ", duration=" + duration + "]";
	}
	
}
