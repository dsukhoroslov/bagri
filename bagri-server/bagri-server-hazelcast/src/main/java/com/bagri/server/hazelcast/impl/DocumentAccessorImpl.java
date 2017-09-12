package com.bagri.server.hazelcast.impl;

import static com.bagri.server.hazelcast.serialize.SystemSerializationFactory.cli_DocumentAccessor;
import static com.bagri.server.hazelcast.serialize.SystemSerializationFactory.cli_factory_id;


import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.bagri.core.api.impl.DocumentAccessorBase;
import com.bagri.core.model.Document;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;

public class DocumentAccessorImpl extends DocumentAccessorBase implements IdentifiedDataSerializable {
	
	private Document doc;
	private Set<String> keys;
	private Map<String, Object> headers;
	
	public DocumentAccessorImpl() {
		// for de-ser
	}
	
	public DocumentAccessorImpl(Document doc, Object content) {
		this.content = content;
		this.doc = doc;
	}

	public DocumentAccessorImpl(Document doc, Set<String> keys, Object content) {
		this.content = content;
		this.keys = new HashSet<>(keys);
	}
	
	public DocumentAccessorImpl(Map<String, Object> headers, Object content) {
		this.headers = new HashMap<>(headers);
		this.keys = this.headers.keySet(); 
		this.content = content;
	}

	@Override
	public String getUri() {
		if (doc != null) {
			return doc.getUri();
		}
		return (String) headers.get(HDR_URI);
	}
	
	public long getDocumentKey() {
		if (doc != null) {
			return doc.getDocumentKey();
		}
		return (Long) headers.get(HDR_KEY);
	}

	@Override
	public int getVersion() {
		if (doc != null) {
			return doc.getVersion();
		}
		return (Integer) headers.get(HDR_VERSION);
	}

	@Override
	public Map<String, Object> getHeaders() {
		if (headers == null) {
			buildHeaders();
		}
		return headers;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T getHeader(String hName) {
		if (doc != null) {
			return (T) getDocumentField(hName);
		}
		return (T) headers.get(hName);
	}

	@Override
	public int getFactoryId() {
		return cli_factory_id;
	}

	@Override
	public int getId() {
		return cli_DocumentAccessor;
	}

	@Override
	public void readData(ObjectDataInput in) throws IOException {
		int size = in.readInt();
		headers = new HashMap<>(size);
		for (int i=0; i < size; i++) {
			headers.put(in.readUTF(), in.readObject());
		}
		content = in.readObject();
	}

	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		if (headers == null && doc != null) {
			buildHeaders();
		}
		if (headers != null) {
			out.writeInt(headers.size());
			for (Map.Entry<String, Object> entry: headers.entrySet()) {
				out.writeUTF(entry.getKey());
				out.writeObject(entry.getValue());
			}
		} else {
			out.writeInt(0);
		}
		out.writeObject(content);
	}
	
	private void buildHeaders() {
		headers = new HashMap<>();
		if (keys != null) {
			for (String key: keys) {
				headers.put(key, getDocumentField(key));
			}
		} else {
			headers.put(HDR_CONTENT_TYPE, doc.getContentType());
			headers.put(HDR_CREATED_AT, doc.getCreatedAt());
			headers.put(HDR_CREATED_BY, doc.getCreatedBy());
			headers.put(HDR_KEY, doc.getDocumentKey());
			headers.put(HDR_TX_START, doc.getTxStart());
			headers.put(HDR_TX_FINISH, doc.getTxFinish());
			headers.put(HDR_URI, doc.getUri());
			headers.put(HDR_VERSION, doc.getVersion());
		}
		keys = headers.keySet();
	}
	
	private Object getDocumentField(String hName) {
		switch (hName) {
			case HDR_COLLECTIONS: return doc.getCollections();
			case HDR_CONTENT_TYPE: return doc.getContentType();
			case HDR_CREATED_AT: return doc.getCreatedAt();
			case HDR_CREATED_BY: return doc.getCreatedBy();
			case HDR_ENCODING: return doc.getEncoding();
			case HDR_FORMAT: return doc.getFormat();
			case HDR_KEY: return doc.getDocumentKey();
			case HDR_SIZE_IN_BYTES: return doc.getBytes();
			case HDR_SIZE_IN_ELEMENTS: return doc.getElements();
			case HDR_SIZE_IN_FRAGMENTS: return doc.getFragments().length;
			case HDR_TYPE_ROOT: return doc.getTypeRoot();
			case HDR_TX_START: return doc.getTxStart();
			case HDR_TX_FINISH: return doc.getTxFinish();
			case HDR_URI: return doc.getUri();
			case HDR_VERSION: return doc.getVersion();
			default: return null; 
		}
	}

}

