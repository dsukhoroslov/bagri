package com.bagri.server.hazelcast.task.doc;

import static com.bagri.client.hazelcast.serialize.DataSerializationFactoryImpl.factoryId;
import static com.bagri.core.server.api.CacheConstants.CN_XDM_DOCUMENT;
import static com.bagri.server.hazelcast.serialize.DataSerializationFactoryImpl.cli_CleanTxDocumentsTask;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.bagri.core.DocumentKey;
import com.bagri.core.api.TransactionState;
import com.bagri.core.model.Document;
import com.bagri.core.model.Transaction;
import com.bagri.server.hazelcast.impl.DocumentManagementImpl;
import com.bagri.server.hazelcast.impl.SchemaRepositoryImpl;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;
import com.hazelcast.query.Predicates;
import com.hazelcast.spring.context.SpringAware;

@SpringAware
public class DocumentCleaner implements Callable<Transaction>, IdentifiedDataSerializable { 
	
	private static final transient Logger logger = LoggerFactory.getLogger(DocumentCleaner.class);

	private Transaction xTx;
	private transient SchemaRepositoryImpl xdmRepo;
    
	public DocumentCleaner() {
		//
	}
	
	public DocumentCleaner(Transaction xTx) {
		this.xTx = xTx;
	}

    @Autowired
	public void setXDMRepository(SchemaRepositoryImpl xdmRepo) {
		this.xdmRepo = xdmRepo;
	}
	
	
	@Override
	public Transaction call() throws Exception {
		logger.trace("call; going to clean documents for tx: {}", xTx);
		HazelcastInstance hz = xdmRepo.getHzInstance();
		IMap<DocumentKey, Document> xddCache = hz.getMap(CN_XDM_DOCUMENT);
		DocumentManagementImpl docMgr = (DocumentManagementImpl) xdmRepo.getDocumentManagement();
		// adjust cleaned counters; updateCounters(created, updated, deleted);
		Set<DocumentKey> dkStarted = xddCache.localKeySet(Predicates.equal("txStart", xTx.getTxId()));
		Set<DocumentKey> dkFinished = xddCache.localKeySet(Predicates.equal("txFinish", xTx.getTxId()));
		Transaction result = new Transaction(xTx.getTxId(), xTx.getStartedAt(), xTx.getFinishedAt(), xTx.getStartedBy(), xTx.getTxIsolation(), xTx.getTxState());

		//logger.debug("call; started: {}; finished: {}", dkStarted, dkFinished);
		//logger.debug("call; all docs: {}", xddCache.values());
		
		boolean commit = xTx.getTxState() == TransactionState.commited;
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
					docMgr.cleanDocument(dk, true); 
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
					logger.trace("call: found deleted documents; started: {}; finished: {}; newkey: {}", dkStarted, dkFinished, nk);
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
