package com.bagri.xdm.client.hazelcast.task.doc;

import static com.bagri.xdm.client.hazelcast.serialize.XDMDataSerializationFactory.factoryId;

import java.io.IOException;

import com.hazelcast.core.PartitionAware;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;

public abstract class DocumentAwareTask implements PartitionAware<Long>, IdentifiedDataSerializable {
	
	protected String clientId;
	protected long docId;
	protected long txId;
	
	public DocumentAwareTask() {
		//
	}
	
	public DocumentAwareTask(String clientId, long docId, long txId) {
		this.clientId = clientId;
		this.docId = docId;
		this.txId = txId;
	}

	@Override
	public Long getPartitionKey() {
		return docId;
	}

	@Override
	public int getFactoryId() {
		return factoryId;
	}

	@Override
	public void readData(ObjectDataInput in) throws IOException {
		clientId = in.readUTF();
		docId = in.readLong();
		txId = in.readLong();
	}

	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		out.writeUTF(clientId);
		out.writeLong(docId);
		out.writeLong(txId);
	}

}


