package com.bagri.client.hazelcast.task.doc;

import static com.bagri.client.hazelcast.serialize.TaskSerializationFactory.cli_RemoveDocumentsTask;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.Callable;

import com.bagri.client.hazelcast.task.TransactionAwareTask;
import com.bagri.core.api.DocumentAccessor;
import com.bagri.core.api.ResultCollection;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;

public class DocumentsRemover extends TransactionAwareTask implements Callable<ResultCollection<DocumentAccessor>>, IdentifiedDataSerializable {
	
	protected String pattern;
	protected Properties props;

	public DocumentsRemover() {
		super();
	}
	
	public DocumentsRemover(String clientId, long txId, String pattern, Properties props) {
		super(clientId, txId);
		this.pattern = pattern;
		this.props = props;
	}

	@Override
	public ResultCollection<DocumentAccessor> call() throws Exception {
		return null;
	}
	
	@Override
	public int getId() {
		return cli_RemoveDocumentsTask;
	}

	@Override
	public void readData(ObjectDataInput in) throws IOException {
		super.readData(in);
		pattern = in.readUTF();
		props = in.readObject(Properties.class);
	}

	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		super.writeData(out);
		out.writeUTF(pattern);
		out.writeObject(props);
	}


}
