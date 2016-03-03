package com.bagri.xdm.cache.hazelcast.predicate;

//import static com.bagri.xdm.client.hazelcast.serialize.XDMDataSerializationFactory.cli_UriPredicate;

import static com.bagri.xdm.client.common.XDMCacheConstants.CN_XDM_DOCUMENT;
import static com.bagri.xdm.client.hazelcast.serialize.XDMDataSerializationFactory.factoryId;

import java.io.IOException;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.xdm.cache.hazelcast.util.HazelcastUtils;
import com.bagri.xdm.common.XDMDocumentKey;
import com.bagri.xdm.common.XDMFactory;
import com.bagri.xdm.domain.XDMDocument;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.PartitionAware;
import com.hazelcast.map.EntryBackupProcessor;
import com.hazelcast.map.EntryProcessor;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;
import com.hazelcast.query.Predicate;

public class UriPredicate implements EntryProcessor<XDMDocumentKey, XDMDocument>, Predicate<XDMDocumentKey, XDMDocument>, PartitionAware<Long>, IdentifiedDataSerializable {

    private static final Logger logger = LoggerFactory.getLogger(UriPredicate.class);
	
    private String uri;
    private long documentId;
    private XDMFactory factory;
    private HazelcastInstance hzInstance;
    
    public UriPredicate() {
    	//
    }
    
    public UriPredicate(long docId, String uri, XDMFactory factory, HazelcastInstance hzInstance) {
    	this.documentId = docId;
    	this.uri = uri;
    	this.factory = factory;
    	this.hzInstance = hzInstance;
    }
    
	@Override
	public boolean apply(Entry<XDMDocumentKey, XDMDocument> mapEntry) {
		logger.info("apply; entry: {}", mapEntry);
		return uri.equals(mapEntry.getValue().getUri());
	}

	@Override
	public Long getPartitionKey() {
		logger.info("getPartitionKey; documentId: {}", documentId);
		return documentId;
	}
	
	@Override
	public int getFactoryId() {
		return factoryId;
	}

	@Override
	public int getId() {
		return 0; //cli_UriPredicate;
	}

	@Override
	public void readData(ObjectDataInput in) throws IOException {
		documentId = in.readLong();
		uri = in.readUTF();
		logger.info("readData; documentId: {}", documentId);
	}

	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		logger.info("writeData; documentId: {}", documentId);
		out.writeLong(documentId);
		out.writeUTF(uri);
	}

	@Override
	public Object process(Entry<XDMDocumentKey, XDMDocument> entry) {
		IMap<XDMDocumentKey, XDMDocument> xddCache = hzInstance.getMap(CN_XDM_DOCUMENT);
		XDMDocument doc = null;
		XDMDocumentKey key = entry.getKey();
		do {
			XDMDocument lastDoc = xddCache.get(key);
			if (lastDoc != null) {
				doc = lastDoc;
				key = factory.newXDMDocumentKey(lastDoc.getDocumentId(), lastDoc.getVersion() + 1);
			} else {
				break;
			}
		} while (true);
		
		if (doc != null) {
			if (doc.getTxFinish() == 0) {
				return doc.getDocumentKey();
			} else {
				logger.info("process; the latest document version is finished already: {}", doc);
			}
		}
		return null;
	}

	@Override
	public EntryBackupProcessor<XDMDocumentKey, XDMDocument> getBackupProcessor() {
		//logger.info("getBackupProcessor; documentId: {}", documentId);
		return null;
	}


}
