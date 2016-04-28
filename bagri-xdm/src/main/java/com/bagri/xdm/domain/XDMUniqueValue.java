package com.bagri.xdm.domain;

public class XDMUniqueValue { 

	private long docKey;
	private long txStart;
	private long txFinish;
	
	public XDMUniqueValue(long docKey, long txStart, long txFinish) {
		this.docKey = docKey;
		this.txStart = txStart;
		this.txFinish = txFinish;
	}
	
	public long getDocumentKey() {
		return docKey;
	}
	
	public long getTxStart() {
		return txStart;
	}
	
	public long getTxFinish() {
		return txFinish;
	}

	@Override
	public String toString() {
		return "XDMUniqueDocument [docKey=" + docKey + ", txStart=" + txStart + ", txFinish=" + txFinish + "]";
	}

}	
