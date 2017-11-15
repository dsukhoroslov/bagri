package com.bagri.client.hazelcast.impl;

import static com.bagri.client.hazelcast.serialize.CompressingSerializer.*;
import static com.bagri.client.hazelcast.serialize.SystemSerializationFactory.cli_CompressingDocumentAccessor;

import java.io.IOException;

import com.bagri.core.api.ContentSerializer;
import com.hazelcast.client.impl.HazelcastClientProxy;
import com.hazelcast.internal.serialization.InternalSerializationService;
import com.hazelcast.nio.ObjectDataInput;

public class CompressingDocumentAccessorImpl extends DocumentAccessorImpl {

	@Override
	public int getId() {
		return cli_CompressingDocumentAccessor;
	}

	protected InternalSerializationService getSerializationService() {
		HazelcastClientProxy proxy = (HazelcastClientProxy) ((SchemaRepositoryImpl) repo).getHazelcastClient();
		return (InternalSerializationService) proxy.getSerializationService();
	}

	@Override
	protected Object readContent(ObjectDataInput in) throws IOException {
		ContentSerializer cs = repo.getSerializer(contentType);
		if (cs != null) {
			return readCompressedContent(getSerializationService(), in, cs);
		} 
		return readCompressedData(getSerializationService(), in);
	}

	
}

