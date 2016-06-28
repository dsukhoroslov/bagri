package com.bagri.xdm.cache.hazelcast.task.doc;

import static com.bagri.xdm.client.hazelcast.serialize.DataSerializationFactoryImpl.factoryId;
import static com.bagri.xdm.cache.api.XDMCacheConstants.CN_XDM_DOCUMENT;
import static com.bagri.xdm.cache.hazelcast.serialize.DataSerializationFactoryImpl.cli_CleanTxDocumentsTask;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.bagri.xdm.api.XDMTransactionState;
import com.bagri.xdm.cache.hazelcast.impl.DocumentManagementImpl;
import com.bagri.xdm.cache.hazelcast.impl.RepositoryImpl;
import com.bagri.xdm.common.DocumentKey;
import com.bagri.xdm.domain.Document;
import com.bagri.xdm.domain.Transaction;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;
import com.hazelcast.query.Predicate;
import com.hazelcast.query.Predicates;
import com.hazelcast.spring.context.SpringAware;

@SpringAware
public class DocumentCleaner implements Callable<Transaction>, IdentifiedDataSerializable { 
	
	private static final transient Logger logger = LoggerFactory.getLogger(DocumentCleaner.class);

	private Transaction xTx;
	private transient RepositoryImpl xdmRepo;
    
	public DocumentCleaner() {
		//
	}
	
	public DocumentCleaner(Transaction xTx) {
		this.xTx = xTx;
	}

    @Autowired
	public void setXDMRepository(RepositoryImpl xdmRepo) {
		this.xdmRepo = xdmRepo;
	}
	
	
	@Override
	public Transaction call() throws Exception {
		HazelcastInstance hz = xdmRepo.getHzInstance();
		IMap<DocumentKey, Document> xddCache = hz.getMap(CN_XDM_DOCUMENT);
		DocumentManagementImpl docMgr = (DocumentManagementImpl) xdmRepo.getDocumentManagement();
		// adjust cleaned counters; updateCounters(created, updated, deleted);
		Set<DocumentKey> dkStarted = xddCache.localKeySet(Predicates.equal("txStart", xTx.getTxId()));
		Set<DocumentKey> dkFinished = xddCache.localKeySet(Predicates.equal("txFinish", xTx.getTxId()));
		Transaction result = new Transaction(xTx.getTxId(), xTx.getStartedAt(), xTx.getFinishedAt(), xTx.getStartedBy(), xTx.getTxIsolation(), xTx.getTxState());
		
		boolean commit = xTx.getTxState() == XDMTransactionState.commited;
		// on commit: 
		//	inserted - do nothing; calc counters 
		//	updated - delete previous version elements, xml and indices, invalidate results (?); 
		//	deleted - the same as for update;
		// on rollback: 
		//	inserted - delete docs with elements, xml and indices, not sure about results;
		//	updated - restore previous doc version (set txFinish to 0), delete new doc version as above;
		//	deleted - the same as for update;
		try {
			for (DocumentKey dk: dkStarted) {
				// previous version for the same doc 
				//XDMDocumentKey pk = docMgr.getXdmFactory().newXDMDocumentKey(dk.getDocumentId(), dk.getVersion() - 1);
				if (dk.getVersion() == Document.dvFirst) {
					// created, for sure
					result.updateCounters(1, 0, 0);
				} else {
					// updated, a new version
					result.updateCounters(0, 1, 0);
				}
				if (!commit) {
					// just delete the doc and all its relatives: elements, content, source, cached result
					docMgr.cleanDocument(dk, true); //false);
				} 
			}
			for (DocumentKey dk: dkFinished) {
				// next version for the same doc 
				DocumentKey nk = xdmRepo.getFactory().newDocumentKey(dk.getKey(), dk.getVersion() + 1);
				if (dkStarted.contains(nk)) {
					//updated, already counted above;
				} else {
					// deleted
					result.updateCounters(0, 0, 1);
					logger.info("call; started: {}; finished: {}; newkey: {}", dkStarted, dkFinished, nk);
				}
				if (commit) {
					// just delete doc relatives: elements, content, source, cached result
					docMgr.cleanDocument(dk, false);
				} else {
					// make doc Active again
					docMgr.rollbackDocument(dk);
				}
			}
		} catch (Throwable ex) {
			logger.error("call.error", ex);
		}
		return result;
	}

	@Override
	public int getFactoryId() {
		return factoryId;
	}

	@Override
	public int getId() {
		return cli_CleanTxDocumentsTask;
	}

	@Override
	public void readData(ObjectDataInput in) throws IOException {
		xTx = in.readObject();
	}

	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		out.writeObject(xTx);
	}

}
