package com.bagri.xdm.domain;

import java.io.Serializable;
import java.util.Date;

public class XDMDocument { //implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 304713901204617098L;
	
	protected long documentId;
	protected String uri;
	protected int typeId;
	protected int version;
	protected Date createdAt;
	protected String createdBy;
	protected String encoding;
	
	public XDMDocument() {
		//
	}
	
	public XDMDocument(long documentId, String uri, int typeId) {
		this.documentId = documentId;
		this.uri = uri;
		this.typeId = typeId;
		this.version = 0;
		this.createdAt = new Date();
		this.createdBy = "system"; // get default user from connection..
		this.encoding = "UTF-8";
	}
	
	public XDMDocument(long documentId, String uri, int typeId, int version, Date createdAt, String createdBy, String encoding) {
		this.documentId = documentId;
		this.uri = uri;
		this.typeId = typeId;
		this.version = version;
		this.createdAt = new Date(createdAt.getTime());
		this.createdBy = createdBy;
		this.encoding = encoding;
	}

	/**
	 * @return the documentId
	 */
	public long getDocumentId() {
		return documentId;
	}

	/**
	 * @return the uri
	 */
	public String getUri() {
		return uri;
	}

	/**
	 * @return the typeId
	 */
	public int getTypeId() {
		return typeId;
	}

	/**
	 * @return the version
	 */
	public int getVersion() {
		return version;
	}

	/**
	 * @return the createdAt
	 */
	public Date getCreatedAt() {
		return createdAt;
	}

	/**
	 * @return the createdBy
	 */
	public String getCreatedBy() {
		return createdBy;
	}

	/**
	 * @return the encoding
	 */
	public String getEncoding() {
		return encoding;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "XDMDocument [documentId=" + documentId + ", uri=" + uri
				+ ", typeId=" + typeId + ", version=" + version 
				+ ", createdAt=" + createdAt + ", createdBy=" + createdBy
				+ ", encoding=" + encoding + "]";
	}
	

	
}
