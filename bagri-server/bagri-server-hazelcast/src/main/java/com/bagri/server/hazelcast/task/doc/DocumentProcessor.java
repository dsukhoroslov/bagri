package com.bagri.server.hazelcast.task.doc;

import static com.bagri.core.Constants.pn_client_storeMode;
import static com.bagri.core.Constants.pv_client_storeMode_insert;
import static com.bagri.core.Constants.pv_client_storeMode_merge;
import static com.bagri.core.api.TransactionManagement.TX_NO;
import static com.bagri.server.hazelcast.serialize.SystemSerializationFactory.cli_factory_id;
import static com.bagri.server.hazelcast.serialize.SystemSerializationFactory.cli_DocumentProcessor;

import java.util.Properties;
import java.io.IOException;
import java.util.Map.Entry;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.bagri.core.DocumentKey;
import com.bagri.core.api.BagriException;
import com.bagri.core.api.TransactionIsolation;
import com.bagri.core.model.Document;
import com.bagri.core.model.ParseResults;
import com.bagri.core.model.Transaction;
import com.bagri.core.server.api.SchemaRepository;
import com.bagri.server.hazelcast.impl.DataDistributionService;
import com.bagri.server.hazelcast.impl.DocumentManagementImpl;
import com.hazelcast.core.Offloadable;
import com.hazelcast.map.EntryBackupProcessor;
import com.hazelcast.map.EntryProcessor;
import com.hazelcast.map.impl.MapEntrySimple;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.IdentifiedDataSerializable;
import com.hazelcast.spring.context.SpringAware;

@SpringAware
public class DocumentProcessor implements EntryProcessor<DocumentKey, Document>, Offloadable, IdentifiedDataSerializable  {

	private static final long serialVersionUID = 1L;

	//private static final transient Logger logger = LoggerFactory.getLogger(DocumentProcessor.class);

	private transient DocumentManagementImpl docMgr;
	private transient DataDistributionService ddSvc;

	private Transaction tx;
	private String uri;
	private String user;
	private Object content;
	private ParseResults data;
	private Properties props;
	
	private Document result;

	public DocumentProcessor() {
		//
	}

	public DocumentProcessor(Transaction tx, String uri, String user, Object content, ParseResults data, Properties props) {
		this.tx = tx;
		this.uri = uri;
		this.user = user;
		this.content = content;
		this.data = data;
		this.props = props;
	}

    @Autowired
	public void setRepository(SchemaRepository repo) {
		//this.repo = repo;
		this.docMgr = (DocumentManagementImpl) repo.getDocumentManagement();
	}

    @Autowired
    public void setDistrService(DataDistributionService ddSvc) {
    	this.ddSvc = ddSvc;
    }

	@Override
	public EntryBackupProcessor<DocumentKey, Document> getBackupProcessor() {
		DocumentBackupProcessor proc = null;
		if (tx == null && result != null) {
			proc = new DocumentBackupProcessor(result);
		}
		//logger.trace("getBackupProcessor.enter; returning {}", proc);
		return proc;
	}

	@Override
	public String getExecutorName() {
		return Offloadable.NO_OFFLOADING; // PN_XDM_SCHEMA_POOL;
	}

	@Override
	public Object process(Entry<DocumentKey, Document> entry) {
		DocumentKey lastKey = null;
		String storeMode = props.getProperty(pn_client_storeMode, pv_client_storeMode_merge); 
		if (!pv_client_storeMode_insert.equals(storeMode)) {
			lastKey = ddSvc.getLastKeyForUri(uri);
		}
		long txStart = tx == null ? TX_NO : tx.getTxId();
		if (lastKey != null && lastKey.getVersion() > entry.getKey().getVersion()) {
			if (txStart > TX_NO && tx.getTxIsolation().ordinal() > TransactionIsolation.readCommited.ordinal()) {
	    		return new BagriException("Document with key: " + entry.getKey() +	", uri: " + uri +
	    				" has been concurrently updated; latest key is: " + lastKey, BagriException.ecDocument);
			}
			Document lastDoc = docMgr.getDocument(lastKey);
			entry = new MapEntrySimple<>(lastKey, lastDoc);
		}
    	try {
    		result = docMgr.processDocument(entry, txStart, uri, user, content, data, props);
    		return result;
    	} catch (BagriException ex) {
    		return ex;
    	}
	}

	@Override
	public int getFactoryId() {
		return cli_factory_id;
	}

	@Override
	public int getId() {
		return cli_DocumentProcessor;
	}

	@Override
	public void readData(ObjectDataInput in) throws IOException {
		tx = in.readObject();
		uri = in.readUTF();
		user = in.readUTF();
		content = in.readObject();
		data = in.readObject();
		props = in.readObject();
	}

	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		out.writeObject(tx);
		out.writeUTF(uri);
		out.writeUTF(user);
		out.writeObject(content);
		out.writeObject(data);
		out.writeObject(props);
	}

}


