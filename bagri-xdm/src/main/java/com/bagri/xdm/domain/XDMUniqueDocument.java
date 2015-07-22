package com.bagri.xdm.domain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.bagri.xdm.api.XDMTransactionManagement;

import static com.bagri.xdm.api.XDMTransactionManagement.TX_NO;


public class XDMUniqueDocument<V> extends XDMIndexedValue<V> {
	
	private List<XDMUniqueValue> docs = new ArrayList<>();
	
	public XDMUniqueDocument() {
		super();
	}
	
	public XDMUniqueDocument(int pathId, V value, long docId) {
		super(pathId, value);
		addDocument(docId, TX_NO);
	}

	public XDMUniqueDocument(int pathId, V value, Collection<Long> docIds) {
		super(pathId, value);
		if (docIds != null) {
			for (Long docId: docIds) {
				addDocument(docId, TX_NO);
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
	public long getDocumentId() {
		for (int i=docs.size() - 1; i >=0; i--) {
			XDMUniqueValue doc = docs.get(i);
			if (doc.getTxFinish() == TX_NO) {
				return doc.getDocumentId();
			}
		}
		return 0;
	}
	
	@Override
	public Set<Long> getDocumentIds() {
		Set<Long> docIds = new HashSet<>(1);
		for (XDMUniqueValue doc: docs) {
			if (doc.getTxFinish() == TX_NO) {
				docIds.add(doc.getDocumentId());
			}
		}
		// returning size must be 1!
		return docIds;
	}
	
	public Collection<XDMUniqueValue> getDocumentValues() {
		return docs;
	}

	@Override
	public boolean addDocument(long docId, long txId) { // synchronized?
		XDMUniqueValue doc;
		for (int i=docs.size() - 1; i >=0; i--) {
			doc = docs.get(i);
			if (doc.getTxFinish() == TX_NO) {
				doc = new XDMUniqueValue(doc.getDocumentId(), doc.getTxStart(), txId);
				docs.set(i, doc);
				break;
			}
		}
		doc = new XDMUniqueValue(docId, txId, TX_NO);
		docs.add(doc);
		return true;
	}

	@Override
	public boolean removeDocument(long docId, long txId) { // synchronized?
		for (int i=docs.size() - 1; i >=0; i--) {
			XDMUniqueValue doc = docs.get(i);
			if (doc.getDocumentId() == docId && doc.getTxFinish() == TX_NO) {
				doc = new XDMUniqueValue(doc.getDocumentId(), doc.getTxStart(), txId);
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
		return "XDMUniqueDocumeny [pathId=" + pathId + "; value=" + value + ";docs=" + docs + "]";
	}

}



