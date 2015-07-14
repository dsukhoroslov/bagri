package com.bagri.xdm.domain;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Denis Sukhoroslov: dsukhoroslov@gmail.com
 * @version 0.3
 */
public class XDMIndexedValue { 

	private Set<Long> docIds = new HashSet<Long>();

	public XDMIndexedValue() {
	}

	public XDMIndexedValue(long docId) {
		this();
		addDocumentId(docId);
	}
	
	public XDMIndexedValue(Collection<Long> docIds) {
		this();
		if (docIds != null) {
			for (Long docId: docIds) {
				addDocumentId(docId);
			}
		}
	}

	public int getCount() {
		return docIds.size();
	}

	/**
	 * @return the documentIds
	 */
	public Set<Long> getDocumentIds() {
		return docIds;
	}

	public boolean addDocumentId(long docId) {
		return docIds.add(docId);
	}
	
	public boolean removeDocumentId(long docId) {
		return docIds.remove(docId);
	}

	@Override
	public String toString() {
		return "XDMIndexedValue [docIds=" + docIds + "]";
	}
	
}
