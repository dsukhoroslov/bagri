package com.bagri.server.hazelcast.predicate;

import static com.bagri.client.hazelcast.serialize.DataSerializationFactoryImpl.cli_DocVisiblePredicate;
import static com.bagri.client.hazelcast.serialize.DataSerializationFactoryImpl.factoryId;
import static com.bagri.core.api.TransactionManagement.TX_NO;

import java.io.IOException;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.bagri.core.DocumentKey;
import com.bagri.core.api.BagriException;
import com.bagri.core.model.Document;
import com.bagri.core.server.api.SchemaRepository;
import com.bagri.server.hazelcast.impl.TransactionManagementImpl;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;
import com.hazelcast.query.Predicate;
import com.hazelcast.spring.context.SpringAware;

@SpringAware
public class DocVisiblePredicate implements Predicate<DocumentKey, Document>, IdentifiedDataSerializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final transient Logger logger = LoggerFactory.getLogger(DocVisiblePredicate.class);
	
	private TransactionManagementImpl txMgr;

    @Autowired
	public void setRepository(SchemaRepository repo) {
		//this.repo = repo;
		this.txMgr = (TransactionManagementImpl) repo.getTxManagement();
	}
	
	@Override
	public boolean apply(Entry<DocumentKey, Document> docEntry) {
		Document doc = docEntry.getValue();
		try {
			// TODO: check start tx too?
			return doc.getTxFinish() == TX_NO || !txMgr.isTxVisible(doc.getTxFinish());
		} catch (BagriException ex) {
			logger.error("apply.error;", ex);
			return false;
		} 
	}

	@Override
	public int getFactoryId() {
		return factoryId;
	}
	
	@Override
	public int getId() {
		return cli_DocVisiblePredicate;
	}

	@Override
	public void readData(ObjectDataInput in) throws IOException {
		// nothing read
	}

	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		// nothing write
	}

}
