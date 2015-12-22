package com.bagri.xdm.cache.hazelcast.task.doc;

import static com.bagri.xdm.client.hazelcast.serialize.XDMDataSerializationFactory.factoryId;
import static com.bagri.xdm.client.hazelcast.serialize.XDMDataSerializationFactory.cli_CleanTxDocumentsTask;

import java.io.IOException;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.bagri.xdm.api.XDMDocumentManagement;
import com.bagri.xdm.domain.XDMTransaction;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;
import com.hazelcast.spring.context.SpringAware;

@SpringAware
public class DocumentCleaner implements Callable<XDMTransaction>, IdentifiedDataSerializable { 
	
	private static final transient Logger logger = LoggerFactory.getLogger(DocumentCleaner.class);

	private XDMTransaction xTx;
	private transient XDMDocumentManagement docMgr;
    
	public DocumentCleaner() {
		//
	}
	
	public DocumentCleaner(XDMTransaction xTx) {
		this.xTx = xTx;
	}

    @Autowired
    @Qualifier("docManager")
	public void setDocManager(XDMDocumentManagement docMgr) {
		this.docMgr = docMgr;
	}
	
	@Override
	public XDMTransaction call() throws Exception {
		// on commit: 
		//	inserted - do nothing; 
		//	updated - delete previous version elements, xml and indices, invalidate results (?); 
		//	deleted - the same as for update;
		// on rollback: 
		//	inserted - delete docs with elements, xml and indices, not sure about results;
		//	updated - restore previous doc version (set txFinish to 0), delete new doc version as above;
		//	deleted - the same as for update;
		XDMTransaction result = new XDMTransaction(xTx.getTxId(), xTx.getStartedAt(), xTx.getFinishedAt(), xTx.getStartedBy(), xTx.getTxIsolation(), xTx.getTxState());
		// adjust cleaned counters; updateCounters(created, updated, deleted);
		result.updateCounters(0, 0, 0);
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
