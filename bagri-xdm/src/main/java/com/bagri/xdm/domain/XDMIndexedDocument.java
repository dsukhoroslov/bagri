package com.bagri.xdm.domain;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class XDMIndexedDocument<V> extends XDMIndexedValue<V> {
	
	private Set<Long> docIds = new HashSet<Long>();

	public XDMIndexedDocument() {
		super();
	}

	public XDMIndexedDocument(int pathId, V value, long docId) {
		super();
	}

	public XDMIndexedDocument(int pathId, V value, Collection<Long> docIds) {
		super();
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
		return "XDMIndexedValue [pathId=" + pathId + "; value=" + value + "; docIds=" + docIds + "]";
	}

}
