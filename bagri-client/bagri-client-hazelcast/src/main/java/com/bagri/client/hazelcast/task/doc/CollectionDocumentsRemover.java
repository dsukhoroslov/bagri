package com.bagri.client.hazelcast.task.doc;

import static com.bagri.client.hazelcast.serialize.DataSerializationFactoryImpl.cli_RemoveCollectionDocumentsTask;

import java.io.IOException;
import java.util.concurrent.Callable;

import com.bagri.client.hazelcast.task.TransactionAwareTask;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;

public class CollectionDocumentsRemover extends TransactionAwareTask implements Callable<Integer>, IdentifiedDataSerializable {
	
	protected String collection;

	public CollectionDocumentsRemover() {
		super();
	}
	
	public CollectionDocumentsRemover(String clientId, long txId, String collection) {
		super(clientId, txId);
		this.collection = collection;
	}

	@Override
	public Integer call() throws Exception {
		return null;
	}
	
	@Override
	public int getId() {
		return cli_RemoveCollectionDocumentsTask;
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
