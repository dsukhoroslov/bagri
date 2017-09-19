package com.bagri.server.hazelcast.task.doc;

import com.bagri.core.DocumentKey;
import com.bagri.core.api.BagriException;
import com.bagri.core.api.TransactionIsolation;
import com.bagri.core.model.Document;
import com.bagri.core.model.Transaction;
import com.bagri.server.hazelcast.impl.DataDistributionService;
import com.hazelcast.core.Offloadable;
import com.hazelcast.map.EntryBackupProcessor;
import com.hazelcast.map.EntryProcessor;
import com.hazelcast.spring.context.SpringAware;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

import static com.bagri.core.api.TransactionManagement.*;

@SpringAware
public class DocumentRemoveProcessor implements EntryProcessor<DocumentKey, Document>, EntryBackupProcessor<DocumentKey, Document>, Offloadable {
    private transient DataDistributionService ddSvc;

    private static final long serialVersionUID = 1L;

    private Transaction tx;

    public DocumentRemoveProcessor(Transaction tx) {
        this.tx = tx;
    }

    @Autowired
    public void setDistrService(DataDistributionService ddSvc) {
        this.ddSvc = ddSvc;
    }

    @Override
    public void processBackup(Map.Entry<DocumentKey, Document> entry) {

    }

    @Override
    public Object process(Map.Entry<DocumentKey, Document> entry) {
        Document doc = entry.getValue();
        DocumentKey lastKey = ddSvc.getLastKeyForUri(doc.getUri());
        long txStart = tx == null ? TX_NO : tx.getTxId();
        if (txStart > TX_NO && tx.getTxIsolation().ordinal() > TransactionIsolation.readCommited.ordinal()) {
            return new BagriException("Document with key: " + entry.getKey() + ", uri: " + doc.getUri() +
                    " has been concurrently updated; latest key is: " + lastKey, BagriException.ecDocument);
        }

        doc.finishDocument(txStart);
        entry.setValue(doc);
        return doc;
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
