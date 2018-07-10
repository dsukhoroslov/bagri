package com.bagri.server.hazelcast.task.index;

import static com.bagri.core.server.api.CacheConstants.CN_XDM_INDEX;
import static com.bagri.server.hazelcast.serialize.TaskSerializationFactory.cli_IndexValuesTask;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.bagri.core.IndexKey;
import com.bagri.core.model.IndexedValue;
import com.bagri.server.hazelcast.impl.DataDistributionService;
import com.hazelcast.map.EntryBackupProcessor;
import com.hazelcast.map.impl.MapEntrySimple;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;

public class ValuesIndexator extends ValueIndexator {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final transient Logger logger = LoggerFactory.getLogger(ValuesIndexator.class);
	
	private Collection<IndexKey> indices;

	private transient DataDistributionService ddSvc;
	
	public ValuesIndexator() {
		//
	}
	
	public ValuesIndexator(long docKey, long txId, Collection<IndexKey> indices) {
		super(docKey, txId);
		this.indices = indices;
	}
	
	@Override
	public int getId() {
		return cli_IndexValuesTask;
	}
	
    @Autowired
    public void setDistrService(DataDistributionService ddSvc) {
    	this.ddSvc = ddSvc;
    }

	@Override
	public EntryBackupProcessor<IndexKey, IndexedValue> getBackupProcessor() {
		return null;
	}

	@Override
	public void processBackup(Entry<IndexKey, IndexedValue> entry) {
		//process(entry);
	}
    
	@Override
	public Object process(Entry<IndexKey, IndexedValue> entry) {
		try {
			for (IndexKey ik: indices) {
				IndexedValue iv = ddSvc.getCachedObject(CN_XDM_INDEX, ik, true);
				processIndex(new MapEntrySimple<>(ik, iv));
			}
		} catch (Exception ex) {
			logger.error("process.error", ex);
			return ex;
		}
		return null;
	}
	
	@Override
	public void readData(ObjectDataInput in) throws IOException {
		super.readData(in);
		int size = in.readInt();
		indices = new ArrayList<>(size);
		for (int i=0; i < size; i++) {
			indices.add((IndexKey) in.readObject());
		}
	}
	
	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		super.writeData(out);
		out.writeInt(indices.size());
		for (IndexKey ik: indices) {
			out.writeObject(ik);
		}
	}
	
}
 


