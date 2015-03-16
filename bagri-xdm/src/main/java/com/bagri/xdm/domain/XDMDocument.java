package com.bagri.xdm.domain;

import java.util.Date;

import com.bagri.xdm.common.XDMEntity;
import static com.bagri.common.util.FileUtils.def_encoding;

public class XDMDocument extends XDMEntity { 

	
	private long documentId;
	private String uri;
	private int typeId;
	private String encoding;
	private long txStart;
	private long txFinish;
	
	public XDMDocument() {
		//
	}
	
	public XDMDocument(long documentId, String uri, int typeId, String owner, long txId) {
		super(0, new Date(), owner);
		this.documentId = documentId;
		this.uri = uri;
		this.typeId = typeId;
		this.txStart = txId;
		this.encoding = def_encoding;
	}
	
	public XDMDocument(long documentId, String uri, int typeId, int version, long txStart, long txFinish, Date createdAt, String createdBy, String encoding) {
		super(version, createdAt, createdBy);
		this.documentId = documentId;
		this.uri = uri;
		this.typeId = typeId;
		this.encoding = encoding;
		this.txStart = txStart;
		this.txFinish = txFinish;
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
	
	public void finishDocument(long txFinish, String by) {
		this.txFinish = txFinish;
		updateVersion(by);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "XDMDocument [documentId=" + documentId + ", uri=" + uri
				+ ", typeId=" + typeId + ", version=" + getVersion()
				+ ", createdAt=" + getCreatedAt() + ", createdBy=" + getCreatedBy()
				+ ", encoding=" + encoding + ", txStart=" + txStart + ", txFinish=" + txFinish + "]";
	}
	

	
}
