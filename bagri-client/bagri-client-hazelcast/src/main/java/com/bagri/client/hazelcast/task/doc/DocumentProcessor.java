package com.bagri.client.hazelcast.task.doc;

import static com.bagri.client.hazelcast.serialize.DataSerializationFactoryImpl.cli_ProcessDocumentTask;

import java.io.IOException;
import java.util.Map.Entry;
import java.util.Properties;

import com.bagri.core.DocumentKey;
import com.bagri.core.model.Document;
import com.hazelcast.core.ReadOnly;
import com.hazelcast.map.EntryBackupProcessor;
import com.hazelcast.map.EntryProcessor;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;

public class DocumentProcessor extends DocumentAwareTask 
	implements EntryProcessor<DocumentKey, Document>, ReadOnly { //EntryBackupProcessor<DocumentKey, Document> {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -3225722672696011111L;
	
	//protected String content;

	public DocumentProcessor() {
		super();
	}

	public DocumentProcessor(String clientId, long txId, String uri, Properties props) {
		super(clientId, txId, uri, props);
	}
	
	//@Override
	//public void processBackup(Entry<Long, Document> entry) {
	//	this.process(entry);
	//}

	@Override
	public Object process(Entry<DocumentKey, Document> entry) {
		return null;
	}

	@Override
	public EntryBackupProcessor<DocumentKey, Document> getBackupProcessor() {
		return null;
	}

	@Override
	public int getId() {
		return cli_ProcessDocumentTask;
	}

	//@Override
	//public void readData(ObjectDataInput in) throws IOException {
	//	super.readData(in);
	//	content = in.readUTF();
	//}

	//@Override
	//public void writeData(ObjectDataOutput out) throws IOException {
	//	super.writeData(out);
	//	out.writeUTF(content);
	//}

}
