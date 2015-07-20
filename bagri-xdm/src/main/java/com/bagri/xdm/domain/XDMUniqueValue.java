package com.bagri.xdm.domain;

public class XDMUniqueValue { 

	private long docId;
	private long txStart;
	private long txFinish;
	
	public XDMUniqueValue(long docId, long txStart, long txFinish) {
		this.docId = docId;
		this.txStart = txStart;
		this.txFinish = txFinish;
	}
	
	public long getDocumentId() {
		return docId;
	}
	
	public long getTxStart() {
		return txStart;
	}
	
	public long getTxFinish() {
		return txFinish;
	}

	@Override
	public String toString() {
		return "XDMUniqueDocument [docId=" + docId + ", txStart=" + txStart + ", txFinish=" + txFinish + "]";
	}

}	
