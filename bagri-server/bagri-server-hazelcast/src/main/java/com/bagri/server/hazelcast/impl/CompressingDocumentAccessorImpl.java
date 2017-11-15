package com.bagri.server.hazelcast.impl;

import static com.bagri.client.hazelcast.serialize.CompressingSerializer.writeCompressedContent;
import static com.bagri.client.hazelcast.serialize.CompressingSerializer.writeCompressedData;
import static com.bagri.client.hazelcast.serialize.SystemSerializationFactory.cli_CompressingDocumentAccessor;

import java.io.IOException;

import com.bagri.server.hazelcast.impl.SchemaRepositoryImpl;
import com.bagri.core.api.ContentSerializer;
import com.bagri.core.api.SchemaRepository;
import com.bagri.core.model.Document;
import com.hazelcast.instance.HazelcastInstanceProxy;
import com.hazelcast.internal.serialization.InternalSerializationService;
import com.hazelcast.nio.ObjectDataOutput;

public class CompressingDocumentAccessorImpl extends DocumentAccessorImpl {

	public CompressingDocumentAccessorImpl() {
		super();
	}
	
	public CompressingDocumentAccessorImpl(SchemaRepository repo, Document doc, long headers, Object content) {
		super(repo, doc, headers, content);
	}
	
	@Override
	public int getId() {
		return cli_CompressingDocumentAccessor;
	}

	protected InternalSerializationService getSerializationService() {
		HazelcastInstanceProxy proxy = (HazelcastInstanceProxy) ((SchemaRepositoryImpl) repo).getHzInstance();
		return (InternalSerializationService) proxy.getSerializationService();
	}

	@Override
	protected void writeContent(ObjectDataOutput out) throws IOException {
		ContentSerializer cs = repo.getSerializer(contentType);
		if (cs != null) {
			writeCompressedContent(getSerializationService(), out, cs, content);
		} else {
			writeCompressedData(getSerializationService(), out, content);
		}
	}

	
}
