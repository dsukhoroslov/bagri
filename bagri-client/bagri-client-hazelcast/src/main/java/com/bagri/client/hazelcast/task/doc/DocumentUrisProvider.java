package com.bagri.client.hazelcast.task.doc;

import static com.bagri.client.hazelcast.serialize.TaskSerializationFactory.cli_ProvideDocumentUrisTask;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.Callable;

import com.bagri.client.hazelcast.task.ContextAwareTask;
import com.bagri.core.api.ResultCollection;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;

public class DocumentUrisProvider extends ContextAwareTask implements Callable<ResultCollection<String>> {
	
	protected String pattern;
	
	public DocumentUrisProvider() {
		super();
	}
	
	public DocumentUrisProvider(String clientId, long txId, Properties props, String pattern) {
		super(clientId, txId, props);
		this.pattern = pattern;
	}

	@Override
	public ResultCollection<String> call() throws Exception {
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
