package com.bagri.client.hazelcast.impl;

import static com.bagri.client.hazelcast.serialize.SystemSerializationFactory.cli_CompressingCollection;

import java.io.IOException;
import java.util.Collection;

import com.bagri.core.api.SchemaRepository;
import com.hazelcast.client.impl.HazelcastClientProxy;
import com.hazelcast.internal.serialization.InternalSerializationService;
import com.hazelcast.nio.IOUtil;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;

public class CompressingCollectionImpl<T> extends FixedCollectionImpl<T> {
	
	protected SchemaRepository repo;
	
	public CompressingCollectionImpl() {
		// de-ser
	}
	
	public CompressingCollectionImpl(SchemaRepository repo, int size) {
		super(size);
		this.repo = repo;
	}

	public CompressingCollectionImpl(SchemaRepository repo, Collection<T> results) {
		super(results);
		this.repo = repo;
	}
	
	@Override
	public int getId() {
		return cli_CompressingCollection;
	}
	
	protected InternalSerializationService getSerializationService() {
		HazelcastClientProxy proxy = (HazelcastClientProxy) ((SchemaRepositoryImpl) repo).getHazelcastClient();
		return (InternalSerializationService) proxy.getSerializationService();
	}

	@Override
	public void readData(ObjectDataInput in) throws IOException {
		repo = SchemaRepositoryImpl.getRepository();
		InternalSerializationService ss = getSerializationService();
		byte[] data = in.readByteArray();
		byte[] data2 = IOUtil.decompress(data);
		//logger.info("readData; compressed size: {}; decompressed size: {}", data.length, data2.length);
		ObjectDataInput odi = ss.createObjectDataInput(data2);
		super.readData(odi);
	}

	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		InternalSerializationService ss = getSerializationService();
		ObjectDataOutput tmp = ss.createObjectDataOutput();
		super.writeData(tmp);
		byte[] data = tmp.toByteArray();
		byte[] data2 = IOUtil.compress(data);
		//logger.info("writeData; original size: {}; compressed size: {}", data.length, data2.length);
		out.writeByteArray(data2);
	}
	
}
