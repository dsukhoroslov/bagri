package com.bagri.client.hazelcast.task.doc;

import static com.bagri.client.hazelcast.serialize.TaskSerializationFactory.cli_StoreDocumentsTask;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Callable;

import com.bagri.client.hazelcast.task.TransactionAwareTask;
import com.bagri.core.api.DocumentAccessor;
import com.bagri.core.api.ResultCollection;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;

public class DocumentsCreator extends TransactionAwareTask implements Callable<ResultCollection<DocumentAccessor>> {

	protected Properties props;
	protected Map<String, Object> documents;

	public DocumentsCreator() {
		super();
	}

	public DocumentsCreator(String clientId, long txId, Map<String, Object> documents, Properties props) {
		super(clientId, txId);
		this.documents = documents;
		this.props = props;
	}

	@Override
	public ResultCollection<DocumentAccessor> call() throws Exception {
		return null; 
	}

	@Override
	public int getId() {
		return cli_StoreDocumentsTask;
	}

	@Override
	public void readData(ObjectDataInput in) throws IOException {
		super.readData(in);
		documents = in.readObject(HashMap.class);
		props = in.readObject(Properties.class);
	}

	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		super.writeData(out);
		out.writeObject(documents);
		out.writeObject(props);
	}



}
