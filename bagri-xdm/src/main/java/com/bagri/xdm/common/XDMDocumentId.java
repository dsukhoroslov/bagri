package com.bagri.xdm.common;

import static com.bagri.xdm.common.XDMDocumentKey.*; 

public class XDMDocumentId {
	
	private long docKey;
	private String uri;
	
	public XDMDocumentId(long docKey) {
		this.docKey = docKey;
	}
	
	public XDMDocumentId(long docKey, String uri) {
		this(docKey);
		this.uri = uri;
	}
	
	public XDMDocumentId(long docId, int version) {
		this.docKey = toKey(docId, version);
	}
	
	public XDMDocumentId(long docId, int version, String uri) {
		this(docId, version);
		this.uri = uri;
	}
	
	public XDMDocumentId(String uri) {
		this.docKey = 0;
		this.uri = uri;
	}
	
	public long getDocumentKey() {
		return docKey;
	}
	
	public long getDocumentId() {
		return toDocumentId(docKey);
	}
	
	public int getVersion() {
		return toVersion(docKey);
	}
	
	public String getDocumentUri() {
		return uri;
	}

	@Override
	public String toString() {
		return "XDMDocumentId [key=" + docKey + " (" + getDocumentId()
			+ ":" + getVersion() + "), uri=" + uri + "]";
	}
	
}
