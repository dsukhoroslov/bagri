package com.bagri.xdm.client.hazelcast.task.doc;

import static com.bagri.xdm.client.hazelcast.serialize.XDMDataSerializationFactory.cli_ProcessDocumentTask;

import java.io.IOException;
import java.util.Map.Entry;

import com.bagri.xdm.domain.XDMDocument;
import com.hazelcast.map.EntryBackupProcessor;
import com.hazelcast.map.EntryProcessor;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;

public class DocumentProcessor extends DocumentAwareTask 
	implements EntryProcessor<Long, XDMDocument>, EntryBackupProcessor<Long, XDMDocument> {
	
	protected String uri;
	protected String xml;

	public DocumentProcessor() {
		super();
	}

	public DocumentProcessor(String clientId, long docId, long txId, String uri, String xml) {
		super(clientId, docId, txId);
		this.uri = uri;
		this.xml = xml;
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
		uri = in.readUTF();
		xml = in.readUTF();
	}

	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		super.writeData(out);
		out.writeUTF(uri);
		out.writeUTF(xml);
	}

}
