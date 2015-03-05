package com.bagri.common.stats;

public class UsageEvent {

	private String name;
	private boolean success;
	private int count;
	
	public UsageEvent(String name, boolean success, int count) {
		this.name = name;
		this.success = success;
		this.count = count;
	}

	public String getName() {
		return name;
	}

	public boolean isSuccess() {
		return success;
	}

	public int getCount() {
		return count;
	}

	@Override
	public String toString() {
		return "UsageEvent [name=" + name + ", success=" + success + ", count="
				+ count + "]";
	}
	
	
}
