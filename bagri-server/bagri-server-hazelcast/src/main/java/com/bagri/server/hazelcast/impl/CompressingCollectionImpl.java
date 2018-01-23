package com.bagri.server.hazelcast.impl;

import java.util.Collection;

import com.bagri.core.api.DocumentAccessor;
import com.bagri.core.api.SchemaRepository;
import com.hazelcast.instance.HazelcastInstanceProxy;
import com.hazelcast.internal.serialization.InternalSerializationService;

public class CompressingCollectionImpl extends com.bagri.client.hazelcast.impl.CompressingCollectionImpl {

	public CompressingCollectionImpl() {
		super();
	}
	
	public CompressingCollectionImpl(SchemaRepository repo, int size) {
		super(repo, size);
	}

	public CompressingCollectionImpl(SchemaRepository repo, Collection<DocumentAccessor> results) {
		super(repo, results);
	}
	
	@Override
	protected InternalSerializationService getSerializationService() {
		HazelcastInstanceProxy proxy = (HazelcastInstanceProxy) ((SchemaRepositoryImpl) repo).getHzInstance();
		return (InternalSerializationService) proxy.getSerializationService();
	}

}
