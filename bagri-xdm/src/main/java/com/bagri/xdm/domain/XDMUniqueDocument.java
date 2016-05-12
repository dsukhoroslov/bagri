package com.bagri.xdm.domain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.bagri.xdm.api.XDMTransactionManagement.TX_NO;


public class XDMUniqueDocument extends XDMIndexedValue {
	
	private List<XDMUniqueValue> docs = new ArrayList<>();
	
	public XDMUniqueDocument() {
		super();
	}
	
	public XDMUniqueDocument(long docKey) {
		super();
		addDocument(docKey, TX_NO);
	}

	public XDMUniqueDocument(Collection<Long> docKeys) {
		super();
		if (docKeys != null) {
			for (Long docKey: docKeys) {
				addDocument(docKey, TX_NO);
			}
		}
	}

	@Override
	public int getCount() {
		int cnt = 0;
		for (int i=docs.size() - 1; i >=0; i--) {
			XDMUniqueValue doc = docs.get(i);
			if (doc.getTxFinish() == TX_NO) {
				cnt++;
			}
		}
		return cnt;
	}
	
	@Override
	public long getDocumentKey() {
		for (int i=docs.size() - 1; i >=0; i--) {
			XDMUniqueValue doc = docs.get(i);
			if (doc.getTxFinish() == TX_NO) {
				return doc.getDocumentKey();
			}
		}
		return 0;
	}
	
	@Override
	public Set<Long> getDocumentKeys() {
		Set<Long> docIds = new HashSet<>(1);
		for (XDMUniqueValue doc: docs) {
			if (doc.getTxFinish() == TX_NO) {
				docIds.add(doc.getDocumentKey());
			}
		}
		// returning size must be 1!
		return docIds;
	}
	
	public Collection<XDMUniqueValue> getDocumentValues() {
		return docs;
	}

	@Override
	public boolean addDocument(long docKey, long txId) { // synchronized?
		XDMUniqueValue doc;
		for (int i=docs.size() - 1; i >=0; i--) {
			doc = docs.get(i);
			if (doc.getTxFinish() == TX_NO) {
				doc = new XDMUniqueValue(doc.getDocumentKey(), doc.getTxStart(), txId);
				docs.set(i, doc);
				break;
			}
		}
		doc = new XDMUniqueValue(docKey, txId, TX_NO);
		docs.add(doc);
		return true;
	}

	@Override
	public boolean removeDocument(long docKey, long txId) { // synchronized?
		for (int i=docs.size() - 1; i >=0; i--) {
			XDMUniqueValue doc = docs.get(i);
			if (doc.getDocumentKey() == docKey && doc.getTxFinish() == TX_NO) {
				doc = new XDMUniqueValue(doc.getDocumentKey(), doc.getTxStart(), txId);
				docs.set(i, doc);
				return true;
			}
		}
		return false;
	}

	public void setDocumentValues(Collection<XDMUniqueValue> values) {
		docs.clear();
		if (values != null) {
			docs.addAll(values);
		}
	}
	
	@Override
	public String toString() {
		return "XDMUniqueDocumeny [docs=" + docs + "]";
	}

	@Override
	public int getSize() {
		// have no idea how much memory ArrayList takes!
		return Long.SIZE / Byte.SIZE // List ref
			+ (4 * Long.SIZE / Byte.SIZE) * docs.size();
	}
	
}



