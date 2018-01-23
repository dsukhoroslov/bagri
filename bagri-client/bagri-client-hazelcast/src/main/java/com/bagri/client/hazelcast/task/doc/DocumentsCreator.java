package com.bagri.client.hazelcast.task.doc;

import static com.bagri.client.hazelcast.serialize.CompressingSerializer.*;
import static com.bagri.client.hazelcast.serialize.TaskSerializationFactory.cli_StoreDocumentsTask;
import static com.bagri.core.Constants.pn_document_compress;
import static com.bagri.core.Constants.pn_document_data_format;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Callable;

import com.bagri.client.hazelcast.impl.SchemaRepositoryImpl;
import com.bagri.client.hazelcast.task.ContextAwareTask;
import com.bagri.core.api.ContentSerializer;
import com.bagri.core.api.ResultCollection;
import com.hazelcast.client.impl.HazelcastClientProxy;
import com.hazelcast.internal.serialization.InternalSerializationService;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;

public class DocumentsCreator extends ContextAwareTask implements Callable<ResultCollection> {

	protected Map<String, Object> documents;

	public DocumentsCreator() {
		super();
	}

	public DocumentsCreator(String clientId, long txId, Properties props, Map<String, Object> documents) {
		super(clientId, txId, props);
		this.documents = documents;
	}

	@Override
	public ResultCollection call() throws Exception {
		return null; 
	}

	@Override
	public int getId() {
		return cli_StoreDocumentsTask;
	}

	protected void checkRepo() {
		// nothing..
	}

	protected InternalSerializationService getSerializationService() {
		HazelcastClientProxy proxy = (HazelcastClientProxy) ((SchemaRepositoryImpl) repo).getHazelcastClient();
		return (InternalSerializationService) proxy.getSerializationService();
	}

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void readData(ObjectDataInput in) throws IOException {
		super.readData(in);
		int size = in.readInt();
		documents = new HashMap<>(size);
		checkRepo();
		String format = context.getProperty(pn_document_data_format);
		boolean compress = Boolean.parseBoolean(context.getProperty(pn_document_compress, "false"));
		if (format != null) {
			ContentSerializer cs = repo.getSerializer(format);
			if (cs != null) {
				if (compress) {
					InternalSerializationService ss = getSerializationService();
					for (int i=0; i < size; i++) {
						documents.put(in.readUTF(), readCompressedContent(ss, in, cs));
					}
				} else {
					for (int i=0; i < size; i++) {
						documents.put(in.readUTF(), cs.readContent(in));
					}
				}
				return;
			}
		} 

		if (compress) {
			InternalSerializationService ss = getSerializationService();
			for (int i=0; i < size; i++) {
				documents.put(in.readUTF(), readCompressedData(ss, in));
			}
		} else {
			for (int i=0; i < size; i++) {
				documents.put(in.readUTF(), in.readObject());
			}
		}
	}

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void writeData(ObjectDataOutput out) throws IOException {
		super.writeData(out);
		out.writeInt(documents.size());
		String format = context.getProperty(pn_document_data_format);
		boolean compress = Boolean.parseBoolean(context.getProperty(pn_document_compress, "false"));
		if (format != null) {
			ContentSerializer cs = repo.getSerializer(format);
			if (cs != null) {
				if (compress) {
					InternalSerializationService ss = getSerializationService();
					for (Map.Entry<String, Object> entry: documents.entrySet()) {
						out.writeUTF(entry.getKey());
						writeCompressedContent(ss, out, cs, entry.getValue());
					}
				} else {
					for (Map.Entry<String, Object> entry: documents.entrySet()) {
						out.writeUTF(entry.getKey());
						cs.writeContent(out, entry.getValue());
					}
				}
				return;
			}
		} 

		if (compress) {
			InternalSerializationService ss = getSerializationService();
			for (Map.Entry<String, Object> entry: documents.entrySet()) {
				out.writeUTF(entry.getKey());
				writeCompressedData(ss, out, entry.getValue());
			}
		} else {
			for (Map.Entry<String, Object> entry: documents.entrySet()) {
				out.writeUTF(entry.getKey());
				out.writeObject(entry.getValue());
			}
		}
		
	}
	
}

