package com.bagri.client.hazelcast.task.doc;

import static com.bagri.client.hazelcast.serialize.TaskSerializationFactory.cli_CreateDocumentTask;
import static com.bagri.core.Constants.pn_document_data_format;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.Callable;

import com.bagri.core.api.ContentSerializer;
import com.bagri.core.api.DocumentAccessor;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;


public class DocumentCreator extends DocumentAwareTask implements Callable<DocumentAccessor> {

	protected Object content;

	public DocumentCreator() {
		super();
	}

	public DocumentCreator(String clientId, long txId, Properties props, String uri, Object content) {
		super(clientId, txId, props, uri);
		this.content = content;
	}

	@Override
	public DocumentAccessor call() throws Exception {
		return null; 
	}

	@Override
	public int getId() {
		return cli_CreateDocumentTask;
	}

	@Override
	public void readData(ObjectDataInput in) throws IOException {
		super.readData(in);
		String format = context.getProperty(pn_document_data_format);
		if (format != null) {
			if (repo != null) {
				ContentSerializer cs = repo.getSerializer(format);
				if (cs != null) {
					content = cs.readContent(in);
					return;
				}
			}
		} 
		content = in.readObject();
	}

	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		out.writeUTF("YCSB");
		super.writeData(out);
		String format = context.getProperty(pn_document_data_format);
		if (format != null) {
			ContentSerializer cs = repo.getSerializer(format);
			if (cs != null) {
				cs.writeContent(out, content);
				return;
			}
		} 
		out.writeObject(content);
	}

}
