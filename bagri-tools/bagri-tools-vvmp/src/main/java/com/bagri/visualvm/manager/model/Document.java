package com.bagri.visualvm.manager.model;

public class Document implements Comparable<Document> {

	private long docKey;
	private String uri;
	
	public Document(long docKey, String uri) {
		this.docKey = docKey;
		this.uri = uri;
	}
	
	public long getDocKey() {
		return docKey;
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
