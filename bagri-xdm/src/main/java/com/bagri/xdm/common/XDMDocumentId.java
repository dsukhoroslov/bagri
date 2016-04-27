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
	
	public int getHash() {
		return toHash(docKey);
	}
	
	public int getRevision() {
		return toRevision(docKey);
	}
	
	public int getVersion() {
		return toVersion(docKey);
	}
	
	public String getDocumentUri() {
		return uri;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (docKey ^ (docKey >>> 32));
		result = prime * result + ((uri == null) ? 0 : uri.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		XDMDocumentId other = (XDMDocumentId) obj;
		if (docKey != other.docKey)
			return false;
		if (uri == null) {
			if (other.uri != null)
				return false;
		} else if (!uri.equals(other.uri))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "XDMDocumentId [key=" + docKey + " (" + getHash() + ":" 
				+ getRevision() + ":" + getVersion() + "), uri=" + uri + "]";
	}
	
}
