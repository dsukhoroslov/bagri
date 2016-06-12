package com.bagri.xdm.domain;

/**
 * 
 * @author Denis Sukhoroslov
 *
 */
public class XDMUniqueValue { 

	private long docKey;
	private long txStart;
	private long txFinish;

	/**
	 * 
	 * @param docKey the internal document key
	 * @param txStart the starting transaction id
	 * @param txFinish the finishing transaction id
	 */
	public XDMUniqueValue(long docKey, long txStart, long txFinish) {
		this.docKey = docKey;
		this.txStart = txStart;
		this.txFinish = txFinish;
	}
	
	/**
	 * 
	 * @return the internal document key
	 */
	public long getDocumentKey() {
		return docKey;
	}
	
	/**
	 * 
	 * @return the starting transaction id
	 */
	public long getTxStart() {
		return txStart;
	}
	
	/**
	 * 
	 * @return the finishing transaction id
	 */
	public long getTxFinish() {
		return txFinish;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "XDMUniqueDocument [docKey=" + docKey + ", txStart=" + txStart + ", txFinish=" + txFinish + "]";
	}

}	
