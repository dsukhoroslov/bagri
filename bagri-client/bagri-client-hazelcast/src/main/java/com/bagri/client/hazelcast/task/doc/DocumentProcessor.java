package com.bagri.client.hazelcast.task.doc;

import java.util.Map.Entry;
import java.util.Properties;

import com.bagri.core.DocumentKey;
import com.bagri.core.api.DocumentDistributionStrategy;
import com.bagri.core.model.Document;
import com.hazelcast.core.Offloadable;
import com.hazelcast.core.ReadOnly;
import com.hazelcast.map.EntryBackupProcessor;
import com.hazelcast.map.EntryProcessor;

public abstract class DocumentProcessor extends DocumentAwareTask implements EntryProcessor<DocumentKey, Document>, ReadOnly, Offloadable { 
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public DocumentProcessor() {
		super();
	}

	public DocumentProcessor(String clientId, long txId, Properties props, String uri, DocumentDistributionStrategy distributor) {
		super(clientId, txId, props, uri, distributor);
	}
	
	@Override
	public Object process(Entry<DocumentKey, Document> entry) {
		return null;
	}

	@Override
	public EntryBackupProcessor<DocumentKey, Document> getBackupProcessor() {
		return null;
	}

	@Override
	public String getExecutorName() {
		return Offloadable.NO_OFFLOADING;
	}

}
