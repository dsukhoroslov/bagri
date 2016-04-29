package com.bagri.xdm.client.hazelcast.task.doc;

import static com.bagri.xdm.client.hazelcast.serialize.XDMDataSerializationFactory.cli_ProcessDocumentTask;

import java.io.IOException;
import java.util.Map.Entry;
import java.util.Properties;

import com.bagri.xdm.domain.XDMDocument;
import com.hazelcast.map.EntryBackupProcessor;
import com.hazelcast.map.EntryProcessor;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;

public class DocumentProcessor extends DocumentAwareTask 
	implements EntryProcessor<Long, XDMDocument>, EntryBackupProcessor<Long, XDMDocument> {
	
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
	public void processBackup(Entry<Long, XDMDocument> entry) {
		this.process(entry);
	}

	@Override
	public Object process(Entry<Long, XDMDocument> entry) {
		return null;
	}

	@Override
	public EntryBackupProcessor<Long, XDMDocument> getBackupProcessor() {
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
