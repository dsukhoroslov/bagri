package com.bagri.xdm.cache.store;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import com.bagri.xdm.common.XDMDocumentKey;
import com.bagri.xdm.domain.XDMDocument;

public interface DocumentStore {
	
	public void init(Map<String, Object> context);

	public XDMDocument loadDocument(XDMDocumentKey key);

	public Map<XDMDocumentKey, XDMDocument> loadAllDocuments(Collection<XDMDocumentKey> keys);

	public Set<XDMDocumentKey> loadAllDocumentKeys();

	public void storeDocument(XDMDocumentKey key, XDMDocument value);

	public void storeAllDocuments(Map<XDMDocumentKey, XDMDocument> entries);

	public void deleteDocument(XDMDocumentKey key);

	public void deleteAllDocuments(Collection<XDMDocumentKey> keys);
		
}
