package com.bagri.common.stats;

public class InvocationEvent {
	
	private String name;
	private boolean success;
	private long duration;
	
	public InvocationEvent(String name, boolean success, long duration) {
		this.name = name;
		this.success = success;
		this.duration = duration;
	}

	public String getName() {
		return name;
	}

	public boolean isSuccess() {
		return success;
	}

	public long getDuration() {
		return duration;
	}

	@Override
	public String toString() {
		return "InvocationEvent [name=" + name + ", success=" + success
				+ ", duration=" + duration + "]";
	}

}
