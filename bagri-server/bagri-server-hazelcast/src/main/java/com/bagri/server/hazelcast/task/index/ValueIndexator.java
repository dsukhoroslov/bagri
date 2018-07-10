package com.bagri.server.hazelcast.task.index;

import static com.bagri.server.hazelcast.serialize.TaskSerializationFactory.cli_factory_id;
import static com.bagri.server.hazelcast.serialize.TaskSerializationFactory.cli_IndexValueTask;

import java.io.IOException;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.bagri.core.IndexKey;
import com.bagri.core.api.BagriException;
import com.bagri.core.model.IndexedValue;
import com.bagri.server.hazelcast.impl.IndexManagementImpl;
import com.hazelcast.core.IMap;
import com.hazelcast.map.EntryBackupProcessor;
import com.hazelcast.map.EntryProcessor;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;
import com.hazelcast.spring.context.SpringAware;

@SpringAware
public class ValueIndexator implements EntryProcessor<IndexKey, IndexedValue>, 
	EntryBackupProcessor<IndexKey, IndexedValue>, IdentifiedDataSerializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final transient Logger logger = LoggerFactory.getLogger(ValueIndexator.class);
	
	protected long docKey;
	protected long txId;
	protected IndexManagementImpl idxMgr;
	
	public ValueIndexator() {
		//
	}

	public ValueIndexator(long docKey, long txId) {
		this.docKey = docKey;
		this.txId = txId;
	}

    @Autowired
	public void setIndexManagement(IndexManagementImpl idxMgr) {
		this.idxMgr = idxMgr;
	}

	@Override
	public int getId() {
		return cli_IndexValueTask;
	}

	@Override
	public int getFactoryId() {
		return cli_factory_id;
	}

	@Override
	public EntryBackupProcessor<IndexKey, IndexedValue> getBackupProcessor() {
		return this;
	}

	@Override
	public Object process(Entry<IndexKey, IndexedValue> entry) {
		try {
			processIndex(entry);
		} catch (BagriException ex) {
			logger.error("process.error", ex);
			return ex;
		}
		return null;
	}

	@Override
	public void processBackup(Entry<IndexKey, IndexedValue> entry) {
		process(entry);
	}

	@Override
	public void readData(ObjectDataInput in) throws IOException {
		docKey = in.readLong();
		txId = in.readLong();
	}

	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		out.writeLong(docKey);
		out.writeLong(txId);
	}

	protected void processIndex(Entry<IndexKey, IndexedValue> entry) throws BagriException {
		IMap<IndexKey, IndexedValue> idxCache = idxMgr.indexPath(entry, docKey, txId);
		if (idxCache != null) {
			// null if no index found
			idxCache.set(entry.getKey(), entry.getValue());
			//ddSvc.storeData(ik, entry.getValue(), CN_XDM_INDEX);
		}
	}

}
