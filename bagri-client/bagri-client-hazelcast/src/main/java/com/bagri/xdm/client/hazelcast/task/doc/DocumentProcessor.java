package com.bagri.xdm.client.hazelcast.task.doc;

import static com.bagri.xdm.client.hazelcast.serialize.XDMDataSerializationFactory.cli_ProcessDocumentTask;

import java.io.IOException;
import java.util.Map.Entry;
import java.util.Properties;

import com.bagri.xdm.common.XDMDocumentId;
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
	
	protected String xml;
	protected Properties props;

	public DocumentProcessor() {
		super();
	}

	public DocumentProcessor(XDMDocumentId docId, String clientId, long txId, String xml, Properties props) {
		super(docId, clientId, txId);
		this.xml = xml;
		this.props = props;
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
		xml = in.readUTF();
		props = in.readObject();
	}

	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		super.writeData(out);
		out.writeUTF(xml);
		out.writeObject(props);
	}

}
