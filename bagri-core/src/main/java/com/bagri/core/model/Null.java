package com.bagri.core.model;

public class Null implements Comparable {
	
	public static final Null _null = new Null();
	
	private Null() {}

	@Override
	public int compareTo(Object o) {
		return -1;
	}
	
	@Override
	public String toString() {
		return "";
	}
	
}

