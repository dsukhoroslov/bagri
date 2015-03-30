package com.bagri.xdm.domain;

import java.util.Date;

import com.bagri.xdm.common.Versionable;

import static com.bagri.xdm.common.XDMDocumentKey.*;
import static com.bagri.common.util.FileUtils.def_encoding;

public class XDMDocument implements Versionable { //extends XDMEntity { 

	private long documentKey;
	private String uri;
	private int typeId;
	private String encoding;
	private long txStart;
	private long txFinish;
	private long createdAt;
	private String createdBy;
	
	public XDMDocument() {
		//
	}
	
	public XDMDocument(long documentId, String uri, int typeId, String owner, long txId) {
		this(documentId, 0, uri, typeId, txId, 0, new Date(), owner, def_encoding);
	}
	
	public XDMDocument(long documentId, int version, String uri, int typeId, String owner, long txId) {
		this(documentId, version, uri, typeId, txId, 0, new Date(), owner, def_encoding);
	}

	public XDMDocument(long documentId, int version, String uri, int typeId, long txStart, long txFinish, Date createdAt, String createdBy, String encoding) {
		//super(version, createdAt, createdBy);
		this.documentKey = toKey(documentId, version);
		this.uri = uri;
		this.typeId = typeId;
		this.encoding = encoding;
		this.txStart = txStart;
		this.txFinish = txFinish;
		this.createdAt = createdAt.getTime();
		this.createdBy = createdBy;
	}

	/**
	 * @return the document Id
	 */
	public long getDocumentId() {
		return toDocumentId(documentKey);
	}

	/**
	 * @return the document key
	 */
	public long getDocumentKey() {
		return documentKey;
	}

	/**
	 * @return the document version
	 */
	public int getVersion() {
		return toVersion(documentKey);
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
	
	/**
	 * @return initiated Tx id
	 */
	public long getTxStart() {
		return txStart;
	}
	
	/**
	 * @return finalized Tx id
	 */
	public long getTxFinish() {
		return txFinish;
	}
	
	@Override
	public Date getCreatedAt() {
		return new Date(createdAt);
	}

	@Override
	public String getCreatedBy() {
		return createdBy;
	}

	@Override
	public void updateVersion(String by) {
		documentKey++; 
		//toKey(getDocumentId(), getVersion() + 1);
		createdAt = System.currentTimeMillis();
		createdBy = by;
	}
	
	public void finishDocument(long txFinish) { //, String by) {
		this.txFinish = txFinish;
		//updateVersion(by);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "XDMDocument [documentId=" + getDocumentId() + ", version=" + getVersion()
				+ ", uri=" + uri + ", typeId=" + typeId + ", createdAt=" + getCreatedAt()
				+ ", createdBy=" + createdBy + ", encoding=" + encoding
				+ ", txStart=" + txStart + ", txFinish=" + txFinish + "]";
	}

	
}
