package com.bagri.client.hazelcast.task.doc;

import static com.bagri.client.hazelcast.serialize.DataSerializationFactoryImpl.cli_ProvideDocumentUrisTask;

import java.io.IOException;
import java.util.Collection;
import java.util.Properties;
import java.util.concurrent.Callable;

import com.bagri.client.hazelcast.task.TransactionAwareTask;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;

public class DocumentUrisProvider extends TransactionAwareTask implements Callable<Collection<String>> {
	
	protected String pattern;
	protected Properties props;
	
	public DocumentUrisProvider() {
		super();
	}
	
	public DocumentUrisProvider(String clientId, long txId, String pattern, Properties props) {
		super(clientId, txId);
		this.pattern = pattern;
		this.props = props;
	}

	@Override
	public Collection<String> call() throws Exception {
		return null;
	}

	@Override
	public int getId() {
		return cli_ProvideDocumentUrisTask;
	}

	@Override
	public void readData(ObjectDataInput in) throws IOException {
		super.readData(in);
		pattern = in.readUTF();
		props = in.readObject();
	}

	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		super.writeData(out);
		out.writeUTF(pattern);
		out.writeObject(props);
	}

}
