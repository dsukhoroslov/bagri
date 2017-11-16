package com.bagri.server.hazelcast.impl;

import static com.bagri.client.hazelcast.serialize.SystemSerializationFactory.cli_CompressingDocumentAccessor;

import java.io.IOException;

import com.bagri.server.hazelcast.impl.SchemaRepositoryImpl;
import com.bagri.core.api.SchemaRepository;
import com.bagri.core.model.Document;
import com.hazelcast.instance.HazelcastInstanceProxy;
import com.hazelcast.internal.serialization.InternalSerializationService;
import com.hazelcast.nio.IOUtil;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;

// implemented to be used in Queue, but it does not show any performance improve
// can be removed in future release
public class CompressingDocumentAccessorImpl extends DocumentAccessorImpl {

	public CompressingDocumentAccessorImpl() {
		super();
	}
	
	public CompressingDocumentAccessorImpl(SchemaRepository repo, Document doc, long headers) {
		super(repo, doc, headers);
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
	public void readData(ObjectDataInput in) throws IOException {
		InternalSerializationService ss = getSerializationService();
		ObjectDataInput odi = ss.createObjectDataInput(IOUtil.decompress(in.readByteArray()));
		super.readData(odi);
	}
	
	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		InternalSerializationService ss = getSerializationService();
		ObjectDataOutput tmp = ss.createObjectDataOutput();
		super.writeData(tmp);
		out.writeByteArray(IOUtil.compress(tmp.toByteArray()));
	}
		
}
