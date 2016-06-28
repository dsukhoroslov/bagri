package com.bagri.xdm.cache.hazelcast.task.index;

import static com.bagri.xdm.cache.hazelcast.serialize.DataSerializationFactoryImpl.cli_IndexValueTask;
import static com.bagri.xdm.client.hazelcast.serialize.DataSerializationFactoryImpl.factoryId;

import java.io.IOException;
import java.util.Map.Entry;

import com.bagri.xdm.api.XDMTransactionManagement;
import com.bagri.xdm.common.IndexKey;
import com.bagri.xdm.domain.IndexedDocument;
import com.bagri.xdm.domain.IndexedValue;
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
			index.addDocument(docId, XDMTransactionManagement.TX_NO);
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
