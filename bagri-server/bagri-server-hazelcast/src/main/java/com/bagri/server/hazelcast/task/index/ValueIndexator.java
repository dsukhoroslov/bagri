package com.bagri.server.hazelcast.task.index;

import static com.bagri.client.hazelcast.serialize.DataSerializationFactoryImpl.factoryId;
import static com.bagri.server.hazelcast.serialize.DataSerializationFactoryImpl.cli_IndexValueTask;

import java.io.IOException;
import java.util.Map.Entry;

import com.bagri.core.IndexKey;
import com.bagri.core.api.TransactionManagement;
import com.bagri.core.model.IndexedDocument;
import com.bagri.core.model.IndexedValue;
import com.hazelcast.map.EntryBackupProcessor;
import com.hazelcast.map.EntryProcessor;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;

public class ValueIndexator implements EntryProcessor<IndexKey, IndexedValue>, 
	EntryBackupProcessor<IndexKey, IndexedValue>, IdentifiedDataSerializable {

	private long docId;
	
	public ValueIndexator() {
		//
	}

	public ValueIndexator(long docId) {
		this.docId = docId;
	}

	@Override
	public int getId() {
		return cli_IndexValueTask;
	}

	@Override
	public int getFactoryId() {
		return factoryId;
	}

	@Override
	public EntryBackupProcessor<IndexKey, IndexedValue> getBackupProcessor() {
		return this;
	}

	@Override
	public Object process(Entry<IndexKey, IndexedValue> entry) {
		IndexedValue index = entry.getValue(); 
		if (index == null) {
			index = new IndexedDocument(docId);
		} else {
			index.addDocument(docId, TransactionManagement.TX_NO);
		}
		entry.setValue(index);
		return null;
	}

	@Override
	public void processBackup(Entry<IndexKey, IndexedValue> entry) {
		process(entry);
	}

	@Override
	public void readData(ObjectDataInput in) throws IOException {
		docId = in.readLong();
	}

	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		out.writeLong(docId);
	}

}
