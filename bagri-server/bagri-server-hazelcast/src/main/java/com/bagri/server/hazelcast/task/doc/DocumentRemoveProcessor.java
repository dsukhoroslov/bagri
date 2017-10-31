package com.bagri.server.hazelcast.task.doc;

import com.bagri.core.DocumentKey;
import com.bagri.core.model.Document;
import com.bagri.core.server.api.SchemaRepository;
import com.bagri.server.hazelcast.impl.DocumentManagementImpl;
import com.hazelcast.core.Offloadable;
import com.hazelcast.map.EntryBackupProcessor;
import com.hazelcast.map.EntryProcessor;
import com.hazelcast.spring.context.SpringAware;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;
import java.util.Properties;

@SpringAware
public class DocumentRemoveProcessor implements EntryProcessor<DocumentKey, Document>, EntryBackupProcessor<DocumentKey, Document>, Offloadable {

    private static final long serialVersionUID = 1L;

    private transient DocumentManagementImpl docMgr;

    private Properties properties;

    public DocumentRemoveProcessor(Properties properties) {
        this.properties = properties;
    }

    @Autowired
    public void setRepository(SchemaRepository repo) {
        this.docMgr = (DocumentManagementImpl) repo.getDocumentManagement();
    }

    @Override
    public void processBackup(Map.Entry<DocumentKey, Document> entry) {

    }

    @Override
    public Object process(Map.Entry<DocumentKey, Document> entry) {

        return docMgr.processDocumentRemoval(entry, properties);
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
