package com.bagri.xdm.domain;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static com.bagri.xdm.api.XDMTransactionManagement.TX_NO;

public class XDMIndexedDocument extends XDMIndexedValue {
	
	private Set<Long> docKeys = new HashSet<>();

	public XDMIndexedDocument() {
		super();
	}

	public XDMIndexedDocument(long docKey) {
		super();
		addDocument(docKey, TX_NO);
	}

	public XDMIndexedDocument(Collection<Long> docKeys) {
		super();
		if (docKeys != null) {
			for (Long docKey: docKeys) {
				addDocument(docKey, TX_NO);
			}
		}
	}

	@Override
	public int getCount() {
		return docKeys.size();
	}

	@Override
	public long getDocumentKey() {
		//if (docIds.size() > 0) {
		//	return ???
		//}
		return 0;
	}

	/**
	 * @return the documentIds
	 */
	@Override
	public Set<Long> getDocumentKeys() {
		return docKeys;
	}

	@Override
	public boolean addDocument(long docKey, long txId) {
		return docKeys.add(docKey);
	}
	
	@Override
	public boolean removeDocument(long docKey, long txId) {
		return docKeys.remove(docKey);
	}

	@Override
	public String toString() {
		return "XDMIndexedValue [docKeys=" + docKeys + "]";
	}

	@Override
	public int getSize() {
		// have no idea how much memory HashSet takes!
		return Long.SIZE / Byte.SIZE // Set ref
			+ (2 * Long.SIZE / Byte.SIZE) * docKeys.size();
	}

}
