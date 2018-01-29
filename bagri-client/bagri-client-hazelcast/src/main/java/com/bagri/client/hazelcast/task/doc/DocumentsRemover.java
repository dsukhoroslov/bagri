package com.bagri.client.hazelcast.task.doc;

import static com.bagri.client.hazelcast.serialize.TaskSerializationFactory.cli_RemoveDocumentsTask;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.Callable;

import com.bagri.client.hazelcast.task.ContextAwareTask;
import com.bagri.core.api.DocumentAccessor;
import com.bagri.core.api.ResultCursor;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;

public class DocumentsRemover extends ContextAwareTask implements Callable<ResultCursor<DocumentAccessor>> { 
	
	protected String pattern;

	public DocumentsRemover() {
		super();
	}
	
	public DocumentsRemover(String clientId, long txId, Properties props, String pattern) {
		super(clientId, txId, props);
		this.pattern = pattern;
	}

	@Override
	public ResultCursor<DocumentAccessor> call() throws Exception {
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
	}

	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		super.writeData(out);
		out.writeUTF(pattern);
	}


}
