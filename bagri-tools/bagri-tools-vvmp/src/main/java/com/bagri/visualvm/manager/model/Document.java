package com.bagri.visualvm.manager.model;

public class Document implements Comparable<Document> {

	private String uri;
	
	public Document(String uri) {
		this.uri = uri;
	}
	
	public String getUri() {
		return uri;
	}
	
	@Override
	public String toString() {
		return uri;
	}

	@Override
	public int compareTo(Document other) {
		return this.uri.toLowerCase().compareTo(other.uri.toLowerCase());
	}

	
}
