package com.bagri.xdm.domain;

import java.io.Serializable;
import java.util.Date;

import com.bagri.xdm.common.XDMEntity;

public class XDMDocument extends XDMEntity { //implements Serializable {

	/**
	 * 
	 */
	//private static final long serialVersionUID = 304713901204617098L;
	
	protected long documentId;
	protected String uri;
	protected int typeId;
	protected String encoding;
	
	public XDMDocument() {
		//
	}
	
	public XDMDocument(long documentId, String uri, int typeId, String owner) {
		super(0, new Date(), owner);
		this.documentId = documentId;
		this.uri = uri;
		this.typeId = typeId;
		this.encoding = "UTF-8";
	}
	
	public XDMDocument(long documentId, String uri, int typeId, int version, Date createdAt, String createdBy, String encoding) {
		super(version, createdAt, createdBy);
		this.documentId = documentId;
		this.uri = uri;
		this.typeId = typeId;
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
				+ ", typeId=" + typeId + ", version=" + getVersion()
				+ ", createdAt=" + getCreatedAt() + ", createdBy=" + getCreatedBy()
				+ ", encoding=" + encoding + "]";
	}
	

	
}
