package com.bagri.client.hazelcast.task.doc;

import static com.bagri.client.hazelcast.serialize.TaskSerializationFactory.cli_UpdateDocumentCollectionTask;

import java.io.IOException;
import java.util.Map.Entry;
import java.util.Properties;

import com.bagri.core.DocumentKey;
import com.bagri.core.model.Document;
import com.bagri.core.server.api.TransactionManagement;
import com.hazelcast.map.EntryBackupProcessor;
import com.hazelcast.map.EntryProcessor;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;

public class DocumentCollectionUpdater extends DocumentAwareTask implements EntryProcessor<DocumentKey, Document>,
	EntryBackupProcessor<DocumentKey, Document>{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	protected boolean add;
	protected String[] collections;
	
	public DocumentCollectionUpdater() {
		super();
	}
	
	public DocumentCollectionUpdater(String clientId, Properties props, String uri, boolean add, String[] collections) {
		super(clientId, TransactionManagement.TX_NO, props, uri);
		this.add = add;
		this.collections = collections;
	}

	@Override
	public int getId() {
		return cli_UpdateDocumentCollectionTask;
	}

	@Override
	public EntryBackupProcessor<DocumentKey, Document> getBackupProcessor() {
		return this;
	}

	@Override
	public Object process(Entry<DocumentKey, Document> entry) {
		return null;
	}

	@Override
	public void processBackup(Entry<DocumentKey, Document> entry) {
	}

	@Override
	public void readData(ObjectDataInput in) throws IOException {
		super.readData(in);
		add = in.readBoolean();
		collections = in.readUTFArray();
	}

	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		super.writeData(out);
		out.writeBoolean(add);
		out.writeUTFArray(collections);
	}

}
