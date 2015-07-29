package com.bagri.xdm.cache.hazelcast.task.index;

import static com.bagri.xdm.client.hazelcast.serialize.XDMDataSerializationFactory.cli_IndexValueTask;
import static com.bagri.xdm.client.hazelcast.serialize.XDMDataSerializationFactory.factoryId;

import java.io.IOException;
import java.util.Map.Entry;

import com.bagri.xdm.api.XDMTransactionManagement;
import com.bagri.xdm.common.XDMIndexKey;
import com.bagri.xdm.domain.XDMIndexedDocument;
import com.bagri.xdm.domain.XDMIndexedValue;
import com.hazelcast.map.EntryBackupProcessor;
import com.hazelcast.map.EntryProcessor;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;

public class ValueIndexator implements EntryProcessor<XDMIndexKey, XDMIndexedValue>, 
	EntryBackupProcessor<XDMIndexKey, XDMIndexedValue>, IdentifiedDataSerializable {

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
	public EntryBackupProcessor<XDMIndexKey, XDMIndexedValue> getBackupProcessor() {
		return this;
	}

	@Override
	public Object process(Entry<XDMIndexKey, XDMIndexedValue> entry) {
		XDMIndexedValue index = entry.getValue(); 
		if (index == null) {
			index = new XDMIndexedDocument(docId);
		} else {
			index.addDocument(docId, XDMTransactionManagement.TX_NO);
		}
		entry.setValue(index);
		return null;
	}

	@Override
	public void processBackup(Entry<XDMIndexKey, XDMIndexedValue> entry) {
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
