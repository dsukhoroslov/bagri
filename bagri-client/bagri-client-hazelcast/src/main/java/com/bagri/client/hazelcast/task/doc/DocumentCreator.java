package com.bagri.client.hazelcast.task.doc;

import static com.bagri.client.hazelcast.serialize.CompressingSerializer.*;
import static com.bagri.client.hazelcast.serialize.TaskSerializationFactory.cli_StoreDocumentTask;
import static com.bagri.core.Constants.pn_document_compress;
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
		return cli_StoreDocumentTask;
	}
	
	protected void checkRepo() {
		// nothing..
	}
	
	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void readData(ObjectDataInput in) throws IOException {
		super.readData(in);
		checkRepo();
		boolean compress = Boolean.parseBoolean(context.getProperty(pn_document_compress, "false"));
		String format = context.getProperty(pn_document_data_format);
		if (format != null) {
			ContentSerializer cs = repo.getSerializer(format);
			if (cs != null) {
				if (compress) {
					content = readCompressedContent(in, cs);
				} else {
					content = cs.readContent(in);
				}
				return;
			}
		} 

		if (compress) {
			content = readCompressedData(in);
		} else {
			content = in.readObject();
		}
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void writeData(ObjectDataOutput out) throws IOException {
		super.writeData(out);
		boolean compress = Boolean.parseBoolean(context.getProperty(pn_document_compress, "false"));
		String format = context.getProperty(pn_document_data_format);
		if (format != null) {
			ContentSerializer cs = repo.getSerializer(format);
			if (cs != null) {
				if (compress) {
					writeCompressedContent(out, cs, content);
				} else {
					cs.writeContent(out, content);
				}
				return;
			}
		} 

		if (compress) {
			writeCompressedData(out, content);
		} else {
			out.writeObject(content);
		}
	}

}
