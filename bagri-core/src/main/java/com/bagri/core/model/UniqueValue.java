package com.bagri.core.model;

/**
 * Represents uniquely indexed document version. A UniqueDocument may contain several UniqueValue (document versions).
 * Only one of then can be active (txFinish == 0).
 * 
 * @author Denis Sukhoroslov
 *
 */
public class UniqueValue { 

	private long docKey;
	private long txStart;
	private long txFinish;

	/**
	 * 
	 * @param docKey the internal document key
	 * @param txStart the starting transaction id
	 * @param txFinish the finishing transaction id
	 */
	public UniqueValue(long docKey, long txStart, long txFinish) {
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
		return "UniqueValue [docKey=" + docKey + ", txStart=" + txStart + ", txFinish=" + txFinish + "]";
	}

}	
