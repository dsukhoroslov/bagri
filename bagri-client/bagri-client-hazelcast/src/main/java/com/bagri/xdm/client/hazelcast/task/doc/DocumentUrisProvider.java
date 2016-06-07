package com.bagri.xdm.client.hazelcast.task.doc;

import static com.bagri.xdm.client.hazelcast.serialize.DataSerializationFactoryImpl.cli_ProvideDocumentUrisTask;

import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.Callable;

import com.bagri.xdm.client.hazelcast.task.TransactionAwareTask;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;

public class DocumentUrisProvider extends TransactionAwareTask implements Callable<Collection<String>> {
	
	protected String pattern;
	
	public DocumentUrisProvider() {
		super();
	}
	
	public DocumentUrisProvider(String clientId, long txId, String pattern) {
		super(clientId, txId);
		this.pattern = pattern;
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
	}

	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		super.writeData(out);
		out.writeUTF(pattern);
	}

}
