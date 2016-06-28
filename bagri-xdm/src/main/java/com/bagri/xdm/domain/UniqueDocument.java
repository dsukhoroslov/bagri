package com.bagri.xdm.domain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.bagri.xdm.api.XDMTransactionManagement.TX_NO;

/**
 * Represents unique index on XDM Document
 * 
 * @author Denis Sukhoroslov
 *
 */
public class UniqueDocument extends IndexedValue {
	
	private List<UniqueValue> docs = new ArrayList<>();
	
	/**
	 * default constructor
	 */
	public UniqueDocument() {
		super();
	}
	
	/**
	 * 
	 * @param docKey the internal document key
	 */
	public UniqueDocument(long docKey) {
		super();
		addDocument(docKey, TX_NO);
	}

	/**
	 * 
	 * @param docKeys the collection of internal document keys
	 */
	public UniqueDocument(Collection<Long> docKeys) {
		super();
		if (docKeys != null) {
			for (Long docKey: docKeys) {
				addDocument(docKey, TX_NO);
			}
		}
	}

	/**
	 * @return the number of indexed documents
	 */
	@Override
	public int getCount() {
		int cnt = 0;
		for (int i=docs.size() - 1; i >=0; i--) {
			UniqueValue doc = docs.get(i);
			if (doc.getTxFinish() == TX_NO) {
				cnt++;
			}
		}
		return cnt;
	}
	
	/**
	 * @return the indexed document key
	 */
	@Override
	public long getDocumentKey() {
		for (int i=docs.size() - 1; i >=0; i--) {
			UniqueValue doc = docs.get(i);
			if (doc.getTxFinish() == TX_NO) {
				return doc.getDocumentKey();
			}
		}
		return 0;
	}
	
	/**
	 * @return the collection of indexed document keys
	 */
	@Override
	public Set<Long> getDocumentKeys() {
		Set<Long> docIds = new HashSet<>(1);
		for (UniqueValue doc: docs) {
			if (doc.getTxFinish() == TX_NO) {
				docIds.add(doc.getDocumentKey());
			}
		}
		// returning size must be 1!
		return docIds;
	}
	
	/**
	 * 
	 * @return the collection of unique indexed values
	 */
	public Collection<UniqueValue> getDocumentValues() {
		return docs;
	}

	/**
	 * adds document to the index
	 * 
	 * @param docKey the internal document key
	 * @param txId the internal transaction id
	 */
	@Override
	public boolean addDocument(long docKey, long txId) { // synchronized?
		UniqueValue doc;
		for (int i=docs.size() - 1; i >=0; i--) {
			doc = docs.get(i);
			if (doc.getTxFinish() == TX_NO) {
				doc = new UniqueValue(doc.getDocumentKey(), doc.getTxStart(), txId);
				docs.set(i, doc);
				break;
			}
		}
		doc = new UniqueValue(docKey, txId, TX_NO);
		docs.add(doc);
		return true;
	}

	/**
	 * removes document from the index 
	 * 
	 * @param docKey the internal document key
	 * @param txId the internal transaction id
	 */
	@Override
	public boolean removeDocument(long docKey, long txId) { // synchronized?
		for (int i=docs.size() - 1; i >=0; i--) {
			UniqueValue doc = docs.get(i);
			if (doc.getDocumentKey() == docKey && doc.getTxFinish() == TX_NO) {
				doc = new UniqueValue(doc.getDocumentKey(), doc.getTxStart(), txId);
				docs.set(i, doc);
				return true;
			}
		}
		return false;
	}

	/**
	 * 
	 * @param values the collection of unique values
	 */
	public void setDocumentValues(Collection<UniqueValue> values) {
		docs.clear();
		if (values != null) {
			docs.addAll(values);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "XDMUniqueDocumeny [docs=" + docs + "]";
	}

	/**
	 * return consumed size in bytes
	 */
	@Override
	public int getSize() {
		// have no idea how much memory ArrayList takes!
		return Long.SIZE / Byte.SIZE // List ref
			+ (4 * Long.SIZE / Byte.SIZE) * docs.size();
	}
	
}



