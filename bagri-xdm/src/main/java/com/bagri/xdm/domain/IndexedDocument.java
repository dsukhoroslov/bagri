package com.bagri.xdm.domain;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static com.bagri.xdm.api.TransactionManagement.TX_NO;

/**
 * Represents indexed document value
 * 
 * @author Denis Sukhoroslov
 *
 */
public class IndexedDocument extends IndexedValue {
	
	private Set<Long> docKeys = new HashSet<>();

	/**
	 * {@inheritDoc}
	 */
	public IndexedDocument() {
		super();
	}

	/**
	 * 
	 * @param docKey the document internal key
	 */
	public IndexedDocument(long docKey) {
		super();
		addDocument(docKey, TX_NO);
	}

	/**
	 * 
	 * @param docKeys the documents
	 */
	public IndexedDocument(Collection<Long> docKeys) {
		super();
		if (docKeys != null) {
			for (Long docKey: docKeys) {
				addDocument(docKey, TX_NO);
			}
		}
	}

	/**
	 * return indexed documents count
	 */
	@Override
	public int getCount() {
		return docKeys.size();
	}

	/**
	 * @return the internal document key
	 */
	@Override
	public long getDocumentKey() {
		return 0;
	}

	/**
	 * @return the document keys
	 */
	@Override
	public Set<Long> getDocumentKeys() {
		return docKeys;
	}

	/**
	 * adds document to index
	 */
	@Override
	public boolean addDocument(long docKey, long txId) {
		return docKeys.add(docKey);
	}
	
	/**
	 * removes document from index
	 */
	@Override
	public boolean removeDocument(long docKey, long txId) {
		return docKeys.remove(docKey);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "IndexedDocument [docKeys=" + docKeys + "]";
	}

	/**
	 * return consumed size in bytes
	 */
	@Override
	public int getSize() {
		// have no idea how much memory HashSet takes!
		return Long.SIZE / Byte.SIZE // Set ref
			+ (2 * Long.SIZE / Byte.SIZE) * docKeys.size();
	}

}
