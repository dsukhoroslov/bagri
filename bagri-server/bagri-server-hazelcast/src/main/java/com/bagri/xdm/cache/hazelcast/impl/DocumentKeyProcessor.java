package com.bagri.xdm.cache.hazelcast.impl;

import static com.bagri.xdm.client.common.XDMCacheConstants.CN_XDM_DOCUMENT;
import static com.bagri.xdm.domain.XDMDocument.dvFirst;

import java.util.Set;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.xdm.common.XDMDocumentKey;
import com.bagri.xdm.common.XDMFactory;
import com.bagri.xdm.domain.XDMDocument;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.map.EntryBackupProcessor;
import com.hazelcast.map.EntryProcessor;
import com.hazelcast.query.Predicate;
import com.hazelcast.query.Predicates;

public class DocumentKeyProcessor implements EntryProcessor<XDMDocumentKey, XDMDocument> { //, IdentifiedDataSerializable {
	
    private static final Logger logger = LoggerFactory.getLogger(DocumentKeyProcessor.class);
	
	private String uri;
	private boolean returnNext;
	private boolean skipClosed;
    private XDMFactory factory;
    private HazelcastInstance hzInstance;
    
    public DocumentKeyProcessor() {
    	//
    }
    
    public DocumentKeyProcessor(String uri, boolean returnNext, boolean skipClosed, XDMFactory factory, HazelcastInstance hzInstance) {
    	this.uri = uri;
    	this.returnNext = returnNext;
    	this.skipClosed = skipClosed;
    	this.factory = factory;
    	this.hzInstance = hzInstance;
    }
    
	@Override
	public Object process(Entry<XDMDocumentKey, XDMDocument> entry) {
		if (entry.getValue() == null) {
			return null;
		}
		
		XDMDocument doc = null;
		boolean sameUri = false;
		XDMDocumentKey key = entry.getKey();
		XDMDocument lastDoc = entry.getValue();
		IMap<XDMDocumentKey, XDMDocument> xddCache = hzInstance.getMap(CN_XDM_DOCUMENT);
		do {
			if (lastDoc != null) {
				doc = lastDoc;
				sameUri = uri.equals(lastDoc.getUri());
				if (sameUri) {
					key = factory.newXDMDocumentKey(uri, key.getRevision(), key.getVersion() + 1);
				} else {
					key = factory.newXDMDocumentKey(uri, key.getRevision() + 1, dvFirst);
				}
				lastDoc = xddCache.get(key);
			} else {
				break;
			}
		} while (true);
		
		if (returnNext) {
			return key;
		}
		if (sameUri) {
			// the txFinish can be > 0, but not committed yet!
	        // should also check if doc's start transaction is committed..
			if (doc.getTxFinish() == 0) {
				return factory.newXDMDocumentKey(doc.getDocumentKey());
			} else {
				if (skipClosed) {
					return key;
				}
				logger.info("process; the latest document version is finished already: {}", doc);
			}
		}
		return null;
	}

	@Override
	public EntryBackupProcessor<XDMDocumentKey, XDMDocument> getBackupProcessor() {
		return null;
	} 

}
