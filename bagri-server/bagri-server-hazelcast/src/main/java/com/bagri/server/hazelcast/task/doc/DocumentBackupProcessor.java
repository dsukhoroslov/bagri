package com.bagri.server.hazelcast.task.doc;

import static com.bagri.client.hazelcast.serialize.SystemSerializationFactory.cli_factory_id;
import static com.bagri.server.hazelcast.serialize.SystemSerializationFactory.cli_DocumentBackupProcessor;

import java.io.IOException;
import java.util.Map.Entry;

import com.bagri.core.DocumentKey;
import com.bagri.core.model.Document;
import com.hazelcast.map.EntryBackupProcessor;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;

public class DocumentBackupProcessor implements EntryBackupProcessor<DocumentKey, Document>, IdentifiedDataSerializable {
	
	//private static final transient Logger logger = LoggerFactory.getLogger(DocumentBackupProcessor.class);
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private Document doc;
	
	public DocumentBackupProcessor() {
		//de-ser
	}

	public DocumentBackupProcessor(Document doc) {
		this.doc = doc;
	}

	@Override
	public void processBackup(Entry<DocumentKey, Document> entry) {
		//logger.trace("processBackup.enter; this: {}; entry: {}", this, entry);
		entry.setValue(doc);
	}

	@Override
	public int getFactoryId() {
		return cli_factory_id;
	}

	@Override
	public int getId() {
		return cli_DocumentBackupProcessor;
	}

	@Override
	public void readData(ObjectDataInput in) throws IOException {
		doc = in.readObject();
	}

	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		out.writeObject(doc);
	}

}
