package com.bagri.xdm.domain;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class XDMUniqueValue {

	private List<XDMUniqueDocument> docs = new ArrayList<>();
	
	public XDMUniqueValue() {
		// de-ser
	}
	
	public int getCount() {
		return 1;
	}
	
	public Set<Long> getDocumentIds() {
		Set<Long> docIds = new HashSet<>(1);
		for (XDMUniqueDocument doc: docs) {
			if (doc.getTxFinish() == 0) {
				docIds.add(doc.getDocumentId());
			}
		}
		return docIds;
	}
	
	public void addDocument(long docId, long txStart) { // synchronized?
		XDMUniqueDocument doc;
		if (docs.size() > 0) {
			int idx = docs.size() - 1;
			doc = docs.get(idx);
			doc = new XDMUniqueDocument(doc.getDocumentId(), doc.getTxStart(), txStart);
			docs.set(idx, doc);
		}
		doc = new XDMUniqueDocument(docId, txStart, 0);
		docs.add(doc);
	}
	
	@Override
	public String toString() {
		return "XDMUniqueValue [docs=" + docs + "]";
	}
	
}
