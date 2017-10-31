package com.bagri.client.hazelcast.task.doc;

import static com.bagri.client.hazelcast.serialize.TaskSerializationFactory.cli_StoreDocumentsTask;
import static com.bagri.core.Constants.pn_document_data_format;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Callable;

import com.bagri.client.hazelcast.task.ContextAwareTask;
import com.bagri.core.api.ContentSerializer;
import com.bagri.core.api.DocumentAccessor;
import com.bagri.core.api.ResultCollection;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;

public class DocumentsCreator extends ContextAwareTask implements Callable<ResultCollection<DocumentAccessor>> {

	protected Map<String, Object> documents;

	public DocumentsCreator() {
		super();
	}

	public DocumentsCreator(String clientId, long txId, Properties props, Map<String, Object> documents) {
		super(clientId, txId, props);
		this.documents = documents;
	}

	@Override
	public ResultCollection<DocumentAccessor> call() throws Exception {
		return null; 
	}

	@Override
	public int getId() {
		return cli_StoreDocumentsTask;
	}

	protected void checkRepo() {
		// nothing..
	}

	@Override
	public void readData(ObjectDataInput in) throws IOException {
		super.readData(in);
		int size = in.readInt();
		documents = new HashMap<>(size);
		checkRepo();
		String format = context.getProperty(pn_document_data_format);
		if (format != null) {
			ContentSerializer cs = repo.getSerializer(format);
			if (cs != null) {
				for (int i=0; i < size; i++) {
					documents.put(in.readUTF(), cs.readContent(in));
				}
				return;
			}
		} 
		for (int i=0; i < size; i++) {
			documents.put(in.readUTF(), in.readObject());
		}
	}

	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		super.writeData(out);
		out.writeInt(documents.size());
		String format = context.getProperty(pn_document_data_format);
		if (format != null) {
			ContentSerializer cs = repo.getSerializer(format);
			if (cs != null) {
				for (Map.Entry<String, Object> entry: documents.entrySet()) {
					out.writeUTF(entry.getKey());
					cs.writeContent(out, entry.getValue());
				}
				return;
			}
		} 
		for (Map.Entry<String, Object> entry: documents.entrySet()) {
			out.writeUTF(entry.getKey());
			out.writeObject(entry.getValue());
		}
	}
	

}
