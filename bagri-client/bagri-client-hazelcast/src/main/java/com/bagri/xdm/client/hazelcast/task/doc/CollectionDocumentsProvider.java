package com.bagri.xdm.client.hazelcast.task.doc;

import static com.bagri.xdm.client.hazelcast.serialize.XDMDataSerializationFactory.cli_ProvideCollectionDocumentsTask;

import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.Callable;

import com.bagri.xdm.client.hazelcast.task.ClientAwareTask;
import com.bagri.xdm.common.XDMDocumentId;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;

public class CollectionDocumentsProvider extends ClientAwareTask implements Callable<Collection<XDMDocumentId>>, IdentifiedDataSerializable {
	
	protected String collection;

	public CollectionDocumentsProvider() {
		super();
	}
	
	public CollectionDocumentsProvider(String clientId, String collection) {
		super(clientId);
		this.collection = collection;
	}

	@Override
	public Collection<XDMDocumentId> call() throws Exception {
		return null;
	}
	
	@Override
	public int getId() {
		return cli_ProvideCollectionDocumentsTask;
	}

	@Override
	public void readData(ObjectDataInput in) throws IOException {
		super.readData(in);
		collection = in.readUTF();
	}

	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		super.writeData(out);
		out.writeUTF(collection);
	}

	
}
