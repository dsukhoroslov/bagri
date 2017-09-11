package com.bagri.server.hazelcast.impl;

import static com.bagri.server.hazelcast.serialize.SystemSerializationFactory.cli_DocumentAccessor;
import static com.bagri.server.hazelcast.serialize.SystemSerializationFactory.cli_factory_id;


import java.io.IOException;
import java.util.Map;

import com.bagri.core.DocumentKey;
import com.bagri.core.api.impl.DocumentAccessorBase;
import com.bagri.core.model.Document;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;

public class DocumentAccessorImpl extends DocumentAccessorBase implements IdentifiedDataSerializable {
	
	private Document doc;
	
	public DocumentAccessorImpl() {
		// for de-ser
	}
	
	public DocumentAccessorImpl(Document doc, Object content) {
		this.content = content;
		this.doc = doc;
	}

	@Override
	public String getUri() {
		return doc.getUri();
	}
	
	public long getDocumentKey() {
		return doc.getDocumentKey();
	}

	@Override
	public int getVersion() {
		return doc.getVersion();
	}

	@Override
	public Map<String, Object> getHeaders() {
		// TODO convert doc to map
		return null;
	}

	@Override
	public <T> T getHeader(String hName) {
		// TODO get doc field
		return null;
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
		// TODO read headers
		content = in.readObject();
	}

	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		// TODO write headers
		out.writeObject(content);
	}

}
