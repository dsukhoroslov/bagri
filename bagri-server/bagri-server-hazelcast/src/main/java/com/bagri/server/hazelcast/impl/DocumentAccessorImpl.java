package com.bagri.server.hazelcast.impl;

import com.bagri.core.model.Document;
import com.bagri.core.api.SchemaRepository;

import static com.bagri.server.hazelcast.serialize.SystemSerializationFactory.*;

public class DocumentAccessorImpl extends com.bagri.client.hazelcast.impl.DocumentAccessorImpl {
	
	public DocumentAccessorImpl() {
		super();
	}
	
	public DocumentAccessorImpl(SchemaRepository repo, int[] collections, Object content, String contentType, long createdAt, String createdBy, 
			String encoding, long docKey, long sizeInBytes, int sizeInElements, int sizeInFragments, String typeRoot, 
			long txStart, long txFinish, String uri, int version) {
		super(repo, collections, content, contentType, createdAt, createdBy, encoding, docKey, sizeInBytes, sizeInElements, sizeInFragments, 
				typeRoot, txStart, txFinish, uri, version);
	}

	public DocumentAccessorImpl(SchemaRepository repo, Document doc, Object content) {
		this(repo, doc.getCollections(), content, doc.getContentType(), doc.getCreatedAt().getTime(), doc.getCreatedBy(), doc.getEncoding(), 
				doc.getDocumentKey(), doc.getBytes(), doc.getElements(), doc.getFragments().length, doc.getTypeRoot(), doc.getTxStart(),
				doc.getTxFinish(), doc.getUri(), doc.getVersion());
	}

	public DocumentAccessorImpl(SchemaRepository repo, Document doc, long headers) {
		this(repo, doc, headers, null);
	}
	
	public DocumentAccessorImpl(SchemaRepository repo, Document doc, long headers, Object content) {
		super();
		this.repo = repo;
		this.headers = headers;
		if ((headers & HDR_COLLECTIONS) != 0) {
			this.collections = doc.getCollections();
		}
		if ((headers & HDR_CONTENT) != 0) {
			this.content = content;
			this.contentType = doc.getContentType();
		} else if ((headers & HDR_CONTENT_TYPE) != 0) {
			this.contentType = doc.getContentType();
		}
		if ((headers & HDR_CREATED_AT) != 0) {
			this.createdAt = doc.getCreatedAt().getTime();
		}
		if ((headers & HDR_CREATED_BY) != 0) {
			this.createdBy = doc.getCreatedBy();
		}
		if ((headers & HDR_ENCODING) != 0) {
			this.encoding = doc.getEncoding();
		}
		if ((headers & HDR_KEY) != 0) {
			this.documentKey = doc.getDocumentKey();
		}
		if ((headers & HDR_SIZE_IN_BYTES) != 0) {
			this.sizeInBytes = doc.getBytes();
		}
		if ((headers & HDR_SIZE_IN_ELEMENTS) != 0) {
			this.sizeInElements = doc.getElements();
		}
		if ((headers & HDR_SIZE_IN_FRAGMENTS) != 0) {
			this.sizeInFragments = doc.getFragments().length;
		}
		if ((headers & HDR_TYPE_ROOT) != 0) {
			this.typeRoot = doc.getTypeRoot();
		}
		if ((headers & HDR_TX_START) != 0) {
			this.txStart = doc.getTxStart();
		}
		if ((headers & HDR_TX_FINISH) != 0) {
			this.txFinish = doc.getTxFinish();
		}
		if ((headers & HDR_URI) != 0) {
			this.uri = doc.getUri();
		}
		if ((headers & HDR_VERSION) != 0) {
			this.version = doc.getVersion();
		}
	}

}

