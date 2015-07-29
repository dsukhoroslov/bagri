package com.bagri.xdm.domain;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static com.bagri.xdm.api.XDMTransactionManagement.TX_NO;

public class XDMIndexedDocument extends XDMIndexedValue {
	
	private Set<Long> docIds = new HashSet<Long>();

	public XDMIndexedDocument() {
		super();
	}

	public XDMIndexedDocument(long docId) {
		super();
		addDocument(docId, TX_NO);
	}

	public XDMIndexedDocument(Collection<Long> docIds) {
		super();
		if (docIds != null) {
			for (Long docId: docIds) {
				addDocument(docId, TX_NO);
			}
		}
	}

	@Override
	public int getCount() {
		return docIds.size();
	}

	@Override
	public long getDocumentId() {
		//if (docIds.size() > 0) {
		//	return ???
		//}
		return 0;
	}

	/**
	 * @return the documentIds
	 */
	@Override
	public Set<Long> getDocumentIds() {
		return docIds;
	}

	@Override
	public boolean addDocument(long docId, long txId) {
		return docIds.add(docId);
	}
	
	@Override
	public boolean removeDocument(long docId, long txId) {
		return docIds.remove(docId);
	}

	@Override
	public String toString() {
		return "XDMIndexedValue [docIds=" + docIds + "]";
	}

}
