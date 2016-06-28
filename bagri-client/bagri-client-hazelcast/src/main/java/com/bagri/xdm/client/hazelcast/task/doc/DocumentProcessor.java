package com.bagri.xdm.client.hazelcast.task.doc;

import static com.bagri.xdm.client.hazelcast.serialize.DataSerializationFactoryImpl.cli_ProcessDocumentTask;

import java.io.IOException;
import java.util.Map.Entry;
import java.util.Properties;

import com.bagri.xdm.domain.Document;
import com.hazelcast.map.EntryBackupProcessor;
import com.hazelcast.map.EntryProcessor;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;

public class DocumentProcessor extends DocumentAwareTask 
	implements EntryProcessor<Long, Document>, EntryBackupProcessor<Long, Document> {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -3225722672696011111L;
	
	protected String content;

	public DocumentProcessor() {
		super();
	}

	public DocumentProcessor(String clientId, long txId, String uri, String content, Properties props) {
		super(clientId, txId, uri, props);
		this.content = content;
	}
	
	@Override
	public void processBackup(Entry<Long, Document> entry) {
		this.process(entry);
	}

	@Override
	public Object process(Entry<Long, Document> entry) {
		return null;
	}

	@Override
	public EntryBackupProcessor<Long, Document> getBackupProcessor() {
		return this;
	}

	@Override
	public int getId() {
		return cli_ProcessDocumentTask;
	}

	@Override
	public void readData(ObjectDataInput in) throws IOException {
		super.readData(in);
		content = in.readUTF();
	}

	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		super.writeData(out);
		out.writeUTF(content);
	}

}
