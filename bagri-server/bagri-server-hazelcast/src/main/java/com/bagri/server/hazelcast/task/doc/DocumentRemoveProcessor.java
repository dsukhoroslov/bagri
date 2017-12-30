package com.bagri.server.hazelcast.task.doc;

import com.bagri.core.DocumentKey;
import com.bagri.core.api.BagriException;
import com.bagri.core.api.TransactionIsolation;
import com.bagri.core.model.Document;
import com.bagri.core.model.Transaction;
import com.bagri.core.server.api.SchemaRepository;
import com.bagri.server.hazelcast.impl.DataDistributionService;
import com.bagri.server.hazelcast.impl.DocumentManagementImpl;
import com.hazelcast.core.Offloadable;
import com.hazelcast.map.EntryBackupProcessor;
import com.hazelcast.map.EntryProcessor;
import com.hazelcast.spring.context.SpringAware;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;
import java.util.Properties;

import static com.bagri.core.api.TransactionManagement.TX_NO;

@SpringAware
public class DocumentRemoveProcessor implements EntryProcessor<DocumentKey, Document>, Offloadable {

    private static final long serialVersionUID = 1L;

    private transient DocumentManagementImpl docMgr;
	private transient DataDistributionService ddSvc;

    private Document doc;
    private Transaction tx;
    private Properties properties;


    public DocumentRemoveProcessor(Transaction tx, Properties properties) {
        this.properties = properties;
        this.tx = tx;
    }

    @Autowired
    public void setRepository(SchemaRepository repo) {
        this.docMgr = (DocumentManagementImpl) repo.getDocumentManagement();
    }

    @Autowired
    public void setDistrService(DataDistributionService ddSvc) {
    	this.ddSvc = ddSvc;
    }

    @Override
    public EntryBackupProcessor<DocumentKey, Document> getBackupProcessor() {
		if (tx == null) {
			doc = null;
		}
		return new DocumentBackupProcessor(doc);
    }

    @Override
    public String getExecutorName() {
        return Offloadable.NO_OFFLOADING;
    }

    @Override
    public Object process(Map.Entry<DocumentKey, Document> entry) {
        doc = entry.getValue();
        long txStart = tx == null ? TX_NO : tx.getTxId();
		DocumentKey lastKey = ddSvc.getLastKeyForUri(doc.getUri());
        if (lastKey == null) {
            return new BagriException("Document with key: " + entry.getKey() + ", uri: " + doc.getUri() +
                    " has been concurrently removed", BagriException.ecDocument);
        } else if (lastKey.getVersion() > entry.getKey().getVersion()) {
            if (txStart > TX_NO && tx.getTxIsolation().ordinal() > TransactionIsolation.readCommited.ordinal()) {
                return new BagriException("Document with key: " + entry.getKey() + ", uri: " + doc.getUri() +
                        " has been concurrently updated; latest key is: " + lastKey, BagriException.ecDocument);
            }
            doc = docMgr.getDocument(lastKey);
        }
        try {
        	return docMgr.processDocumentRemoval(entry, properties, txStart, doc);
        } catch (BagriException ex) {
        	return ex;
        }
    }

}
