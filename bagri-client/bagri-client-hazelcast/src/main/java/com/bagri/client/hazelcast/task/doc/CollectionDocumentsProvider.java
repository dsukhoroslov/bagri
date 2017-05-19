package com.bagri.client.hazelcast.task.doc;

import static com.bagri.client.hazelcast.serialize.DataSerializationFactoryImpl.cli_ProvideCollectionDocumentsTask;

import java.io.IOException;
import java.util.Collection;
import java.util.Properties;
import java.util.concurrent.Callable;

import com.bagri.client.hazelcast.task.ClientAwareTask;
import com.bagri.client.hazelcast.task.TransactionAwareTask;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;

public class CollectionDocumentsProvider extends TransactionAwareTask implements Callable<Collection<String>> {
	
	protected String collection;
	protected Properties props;
	
	public CollectionDocumentsProvider() {
		super();
	}
	
	public CollectionDocumentsProvider(String clientId, long txId, String collection, Properties props) {
		super(clientId, txId);
		this.collection = collection;
		this.props = props;
	}

	@Override
	public Collection<String> call() throws Exception {
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
		props = in.readObject();
	}

	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		super.writeData(out);
		out.writeUTF(collection);
		out.writeObject(props);
	}

	
}
