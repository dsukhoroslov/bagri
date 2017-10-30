package com.bagri.server.hazelcast.task.doc;

import com.bagri.core.DocumentKey;
import com.bagri.core.api.BagriException;
import com.bagri.core.api.DocumentAccessor;
import com.bagri.core.api.TransactionIsolation;
import com.bagri.core.model.Document;
import com.bagri.core.model.Transaction;
import com.bagri.core.server.api.SchemaRepository;
import com.bagri.server.hazelcast.impl.DataDistributionService;
import com.bagri.server.hazelcast.impl.DocumentAccessorImpl;
import com.bagri.server.hazelcast.impl.DocumentManagementImpl;
import com.hazelcast.core.Offloadable;
import com.hazelcast.map.EntryBackupProcessor;
import com.hazelcast.map.EntryProcessor;
import com.hazelcast.spring.context.SpringAware;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;
import java.util.Properties;

import static com.bagri.core.Constants.*;
import static com.bagri.core.api.TransactionManagement.*;

@SpringAware
public class DocumentRemoveProcessor implements EntryProcessor<DocumentKey, Document>, EntryBackupProcessor<DocumentKey, Document>, Offloadable {
    private transient DataDistributionService ddSvc;
    private transient DocumentManagementImpl docMgr;

    private static final long serialVersionUID = 1L;

    private Transaction tx;
    private Properties properties;

    public DocumentRemoveProcessor(Transaction tx, Properties properties) {
        this.tx = tx;
        this.properties = properties;
    }

    @Autowired
    public void setDistrService(DataDistributionService ddSvc) {
        this.ddSvc = ddSvc;
    }

    @Autowired
    public void setRepository(SchemaRepository repo) {
        //this.repo = repo;
        this.docMgr = (DocumentManagementImpl) repo.getDocumentManagement();
    }

    @Override
    public void processBackup(Map.Entry<DocumentKey, Document> entry) {

    }

    @Override
    public Object process(Map.Entry<DocumentKey, Document> entry) {
        Document doc = entry.getValue();
        DocumentKey lastKey = ddSvc.getLastKeyForUri(doc.getUri());
        if (lastKey == null) {
            return new BagriException("Document with uri: " + doc.getUri() + " was not found", BagriException.ecDocument);
        }
        long txStart = tx == null ? TX_NO : tx.getTxId();
        if (lastKey.getVersion() > entry.getKey().getVersion()) {
                if (txStart > TX_NO && tx.getTxIsolation().ordinal() > TransactionIsolation.readCommited.ordinal()) {
                    return new BagriException("Document with key: " + entry.getKey() + ", uri: " + doc.getUri() +
                        " has been concurrently updated; latest key is: " + lastKey, BagriException.ecDocument);
            }
            doc = docMgr.getDocument(lastKey);
        }
        if (txStart == TX_NO) {
            doc = null;
            docMgr.deleteDocumentFromContentCache(entry);
        } else {
            doc.finishDocument(txStart);
        }
        entry.setValue(doc);
        String headers = properties.getProperty(pn_document_headers, String.valueOf(DocumentAccessor.HDR_CLIENT_DOCUMENT));
        long headMask = Long.parseLong(headers);
        if ((headMask & DocumentAccessor.HDR_CONTENT) != 0) {
            return new DocumentAccessorImpl(doc, headMask, docMgr.getDocumentContent(entry.getKey()));
        }
        return new DocumentAccessorImpl(doc, headMask);
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
