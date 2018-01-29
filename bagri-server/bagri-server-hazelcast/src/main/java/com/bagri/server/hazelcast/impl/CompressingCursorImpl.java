package com.bagri.server.hazelcast.impl;

import java.util.Collection;

import com.bagri.core.api.SchemaRepository;
import com.hazelcast.instance.HazelcastInstanceProxy;
import com.hazelcast.internal.serialization.InternalSerializationService;

public class CompressingCursorImpl<T> extends com.bagri.client.hazelcast.impl.CompressingCursorImpl<T> {

	public CompressingCursorImpl() {
		super();
	}
	
	public CompressingCursorImpl(SchemaRepository repo, int size) {
		super(repo, size);
	}

	public CompressingCursorImpl(SchemaRepository repo, Collection<T> results) {
		super(repo, results);
	}
	
	@Override
	protected InternalSerializationService getSerializationService() {
		HazelcastInstanceProxy proxy = (HazelcastInstanceProxy) ((SchemaRepositoryImpl) repo).getHzInstance();
		return (InternalSerializationService) proxy.getSerializationService();
	}

}
