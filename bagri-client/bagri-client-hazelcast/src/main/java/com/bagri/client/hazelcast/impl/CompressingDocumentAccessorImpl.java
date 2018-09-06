package com.bagri.client.hazelcast.impl;

import static com.bagri.client.hazelcast.serialize.SystemSerializationFactory.cli_CompressingDocumentAccessor;

import java.io.IOException;

import com.hazelcast.client.impl.HazelcastClientProxy;
import com.hazelcast.internal.serialization.InternalSerializationService;
import com.hazelcast.nio.IOUtil;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;

public class CompressingDocumentAccessorImpl extends DocumentAccessorImpl {

	@Override
	public int getId() {
		return cli_CompressingDocumentAccessor;
	}

	//protected InternalSerializationService getSerializationService() {
	//	HazelcastClientProxy proxy = (HazelcastClientProxy) ((SchemaRepositoryImpl) repo).getHazelcastClient();
	//	return (InternalSerializationService) proxy.getSerializationService();
	//}

	@Override
	public void readData(ObjectDataInput in) throws IOException {
		//repo = SchemaRepositoryImpl.getRepository();
		InternalSerializationService ss = in.getSerializationService();
		ObjectDataInput odi = ss.createObjectDataInput(IOUtil.decompress(in.readByteArray()));
		super.readData(odi);
	}
	
	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		InternalSerializationService ss = out.getSerializationService();
		ObjectDataOutput tmp = ss.createObjectDataOutput();
		super.writeData(tmp);
		out.writeByteArray(IOUtil.compress(tmp.toByteArray()));
	}
	
}

