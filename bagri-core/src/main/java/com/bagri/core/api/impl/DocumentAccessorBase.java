package com.bagri.core.api.impl;

//import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.core.api.DocumentAccessor;
import com.bagri.core.api.SchemaRepository;

public class DocumentAccessorBase implements DocumentAccessor {

    protected Logger logger = LoggerFactory.getLogger(getClass());

	protected SchemaRepository repo;
	
    protected long headers;

	protected int[] collections;
	protected Object content;
	protected String contentType;
	protected long createdAt;
	protected String createdBy;
	protected String encoding;
	protected long documentKey;
	protected long sizeInBytes;
	protected int sizeInElements;
	protected int sizeInFragments;
	protected String typeRoot;
	protected long txStart = -1;
	protected long txFinish = -1;
	protected String uri;
	protected int version;
	
	//public DocumentAccessorBase() {
		// for de-ser..
	//}

	public DocumentAccessorBase(SchemaRepository repo) {
		this.repo = repo;
	}

	public DocumentAccessorBase(SchemaRepository repo, Object content) {
		this(repo);
		headers = HDR_CONTENT;
		this.content = content;
	}
	
	public DocumentAccessorBase(SchemaRepository repo, int[] collections, Object content, String contentType, long createdAt, String createdBy, 
			String encoding, long documentKey, long sizeInBytes, int sizeInElements, int sizeInFragments, String typeRoot, 
			long txStart, long txFinish, String uri, int version) {
		this(repo);
		if (collections != null) {
			this.collections = collections;
			headers = 1L;
		}
		if (content != null) {
			this.content = content;
			headers |= HDR_CONTENT;
		}
		if (contentType != null) {
			this.contentType = contentType;
			headers |= HDR_CONTENT_TYPE;
		}
		if (createdAt != 0) {
			this.createdAt = createdAt;
			headers |= HDR_CREATED_AT;
		}
		if (createdBy != null) {
			this.createdBy = createdBy;
			headers |= HDR_CREATED_BY;
		}
		if (encoding != null) {
			this.encoding = encoding;
			headers |= HDR_ENCODING;
		}
		if (documentKey != 0) {
			this.documentKey = documentKey;
			headers |= HDR_KEY;
		}
		if (sizeInBytes != 0) {
			this.sizeInBytes = sizeInBytes;
			headers |= HDR_SIZE_IN_BYTES;
		}
		if (sizeInElements != 0) {
			this.sizeInElements = sizeInElements;
			headers |= HDR_SIZE_IN_ELEMENTS;
		}
		if (sizeInFragments != 0) {
			this.sizeInFragments = sizeInFragments;
			headers |= HDR_SIZE_IN_FRAGMENTS;
		}
		if (typeRoot != null) {
			this.typeRoot = typeRoot;
			headers |= HDR_TYPE_ROOT;
		}
		if (txStart >= 0) {
			this.txStart = txStart;
			headers |= HDR_TX_START;
		}
		if (txFinish >= 0) {
			this.txFinish = txFinish;
			headers |= HDR_TX_FINISH;
		}
		if (uri != null) {
			this.uri = uri;
			headers |= HDR_URI;
		}
		if (version != 0) {
			this.version = version;
			headers |= HDR_VERSION;
		}
	}

	@Override
	public int[] getCollections() {
		return collections;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T getContent() {
		return (T) content;
	}

	@Override
	public String getContentType() {
		return contentType;
	}
	
	@Override
	public long getCreateadAt() {
		return createdAt;
	}
	
	@Override
	public String getCreatedBy() {
		return createdBy;
	}
	
	@Override
	public String getEncoding() {
		return encoding;
	}
	
	@Override
	public long getDocumentKey() {
		return documentKey;
	}
	
	@Override
	public long getSizeInBytes() {
		return sizeInBytes;
	}
	
	@Override
	public int getSizeInElements() {
		return sizeInElements;
	}
	
	@Override
	public int getSizeInFragments() {
		return sizeInFragments;
	}
	
	@Override
	public String getTypeRoot() {
		return typeRoot;
	}
	
	@Override
	public long getTxStart() {
		return txStart;
	}
	
	@Override
	public long getTxFinish() {
		return txFinish;
	}
	
	@Override
	public String getUri() {
		return uri;
	}
	
	@Override
	public int getVersion() {
		return version;
	}

	@Override
	public long getHeaders() {
		return headers;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public <T> T getHeader(byte header) {
		switch (header) {
			case IDX_COLLECTIONS: return (T) collections; 
			case IDX_CONTENT: return (T) content;
			case IDX_CONTENT_TYPE: return (T) contentType;
			case IDX_CREATED_AT: return (T) (Long) createdAt;
			case IDX_CREATED_BY: return (T) createdBy;
			case IDX_ENCODING: return (T) encoding;
			case IDX_KEY: return (T) (Long) documentKey;
			case IDX_SIZE_IN_BYTES: return (T) (Long) sizeInBytes;
			case IDX_SIZE_IN_ELEMENTS: return (T) (Integer) sizeInElements;
			case IDX_SIZE_IN_FRAGMENTS: return (T) (Integer) sizeInFragments;
			case IDX_TYPE_ROOT: return (T) typeRoot;
			case IDX_TX_START: return (T) (Long) txStart;
			case IDX_TX_FINISH: return (T) (Long) txFinish;
			case IDX_URI: return (T) uri;
			case IDX_VERSION: return (T) (Integer) version;
		}
		return null;
	}
	
	@Override
	public boolean isHeaderPresent(byte header) {
		long h = 1L << header;
		return (headers & h) == h;
	}
	
	//@Override
	//public Properties getProperties() {
		// for future use
	//	return null;
	//}

	//@Override
	//public String getProperty(String pName) {
		// for future use
	//	return null;
	//}

}
