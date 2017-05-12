package com.bagri.server.hazelcast.task.doc;

import static com.bagri.client.hazelcast.serialize.DataSerializationFactoryImpl.cli_ProcessDocumentTask;

import java.util.Properties;
import java.util.List;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.bagri.core.DocumentKey;
import com.bagri.core.api.BagriException;
import com.bagri.core.model.Data;
import com.bagri.core.model.Document;
import com.bagri.core.server.api.SchemaRepository;
import com.bagri.server.hazelcast.impl.DocumentManagementImpl;
import com.hazelcast.map.EntryBackupProcessor;
import com.hazelcast.map.EntryProcessor;
import com.hazelcast.spring.context.SpringAware;

@SpringAware
public class DocumentProcessor implements EntryProcessor<DocumentKey, Document>, EntryBackupProcessor<DocumentKey, Document> {

	private static final long serialVersionUID = 1L;

	private static final transient Logger logger = LoggerFactory.getLogger(DocumentProcessor.class);
	
	private transient DocumentManagementImpl docMgr;
	
	private long txId;
	private String uri;
	private Object content;
	private List<Data> data;
	private int[] collections;

	public DocumentProcessor() {
		//
	}

	public DocumentProcessor(long txId, String uri, Object content, List<Data> data, int[] collections) {
		this.txId = txId;
		this.uri = uri;
		this.content = content;
		this.data = data;
		this.collections = collections;
	}

    @Autowired
	public void setRepository(SchemaRepository repo) {
		//this.repo = repo;
		this.docMgr = (DocumentManagementImpl) repo.getDocumentManagement();
	}
	
	
	@Override
	public void processBackup(Entry<DocumentKey, Document> entry) {
		//this.process(entry);
	}

	@Override
	public Object process(Entry<DocumentKey, Document> entry) {
		//return null;
    	try {
    		return docMgr.processDocument(entry, txId, uri, content, data, collections);
    	} catch (BagriException ex) {
    		return ex;
    	}
	}

	@Override
	public EntryBackupProcessor<DocumentKey, Document> getBackupProcessor() {
		return null;
	}

	//@Override
	//public int getId() {
	//	return cli_ProcessDocumentTask;
	//}

	//@Override
	//public void readData(ObjectDataInput in) throws IOException {
	//	super.readData(in);
	//	content = in.readUTF();
	//}

	//@Override
	//public void writeData(ObjectDataOutput out) throws IOException {
	//	super.writeData(out);
	//	out.writeUTF(content);
	//}

}
	

