package com.bagri.xdm.common;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import com.bagri.xdm.domain.XDMDocument;

public interface XDMDocumentStore {
	
	public void init(Map<String, Object> context);

	public XDMDocument loadDocument(XDMDocumentKey key);

	public Map<XDMDocumentKey, XDMDocument> loadAllDocuments(Collection<XDMDocumentKey> keys);

	public Set<XDMDocumentKey> loadAllDocumentKeys();

	public void storeDocument(XDMDocumentKey key, XDMDocument value);

	public void storeAllDocuments(Map<XDMDocumentKey, XDMDocument> entries);

	public void deleteDocument(XDMDocumentKey key);

	public void deleteAllDocuments(Collection<XDMDocumentKey> keys);
		
}
