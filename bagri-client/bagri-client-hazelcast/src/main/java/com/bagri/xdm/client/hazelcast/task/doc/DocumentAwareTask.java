package com.bagri.xdm.client.hazelcast.task.doc;

import static com.bagri.xdm.client.hazelcast.serialize.XDMDataSerializationFactory.factoryId;

import java.io.IOException;

import com.bagri.xdm.common.XDMDocumentId;
import com.hazelcast.core.PartitionAware;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;

public abstract class DocumentAwareTask implements PartitionAware<Long>, IdentifiedDataSerializable {
	
	protected String clientId;
	protected long txId;
	protected XDMDocumentId docId;
	
	public DocumentAwareTask() {
		//
	}
	
	public DocumentAwareTask(XDMDocumentId docId, String clientId, long txId) {
		this.docId = docId;
		this.clientId = clientId;
		this.txId = txId;
	}

	@Override
	public Long getPartitionKey() {
		return docId.getDocumentId();
	}

	@Override
	public int getFactoryId() {
		return factoryId;
	}

	@Override
	public void readData(ObjectDataInput in) throws IOException {
		docId = in.readObject();
		clientId = in.readUTF();
		txId = in.readLong();
	}

	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		out.writeObject(docId);
		out.writeUTF(clientId);
		out.writeLong(txId);
	}

}


