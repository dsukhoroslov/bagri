package com.bagri.xdm.cache.hazelcast.management;

import java.io.File;
import java.io.IOException;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.jmx.export.naming.SelfNaming;

import com.bagri.common.manage.JMXUtils;
import com.bagri.common.util.FileUtils;
import com.bagri.xdm.XDMDocument;
import com.bagri.xdm.access.api.XDMDocumentManagerServer;
import com.bagri.xdm.access.api.XDMSchemaDictionary;
import com.bagri.xdm.process.hazelcast.HazelcastDocumentServer;

@ManagedResource(description="Schema Manager MBean")
public class DocumentManagement implements SelfNaming {

    private static final transient Logger logger = LoggerFactory.getLogger(DocumentManagement.class);
    private static final String type_schema = "Schema";
    
	private XDMDocumentManagerServer docManager;
	private XDMSchemaDictionary schemaDictionary;
    
    private String schemaName;
    
    public DocumentManagement(String schemaName) {
    	this.schemaName = schemaName;
    }

	public void setDocumentManager(XDMDocumentManagerServer docManager) {
		this.docManager = docManager;
	}
	
	public void setSchemaDictionary(XDMSchemaDictionary schemaDictionary) {
		this.schemaDictionary = schemaDictionary;
	}
	
	@ManagedAttribute(description="Returns corresponding Schema name")
	public String getSchema() {
		return schemaName;
	}
    
	@ManagedOperation(description="Register Document")
	@ManagedOperationParameters({
		@ManagedOperationParameter(name = "docFile", description = "A full path to XML file to register")})
	public int registerDocument(String docFile) {
		
		String uri = "file:///" + docFile;
		//logger.trace("storeDocument; document initialized: {}", docId);
		//DocumentCreator task = new DocumentCreator(docId, uri, xml);

		try {
			String xml = FileUtils.readTextFile(docFile);
			XDMDocument doc = ((HazelcastDocumentServer) docManager).createDocument(uri, xml);
			return 1;
		} catch (IOException ex) {
			logger.error("registerDocument.error: " + ex.getMessage(), ex);
		}
		return 0;
	}
	
	private int processFilesInCatalog(File catalog) {
		int result = 0;
	    for (File file: catalog.listFiles()) {
	        if (file.isDirectory()) {
	            result += processFilesInCatalog(file);
	        } else {
	            result += registerDocument(file.getPath());
	        }
	    }
	    return result;
	}

	@ManagedOperation(description="Register Documents")
	@ManagedOperationParameters({
		@ManagedOperationParameter(name = "docCatalog", description = "A full path to the directory containing XML files to register")})
	public int registerDocuments(String docCatalog) {
		File catalog = new File(docCatalog);
		return processFilesInCatalog(catalog);	
	}

	@Override
	public ObjectName getObjectName() throws MalformedObjectNameException {
		return JMXUtils.getObjectName(type_schema + "=Schema,name=" + schemaName + ",kind=DocumentManagement");
	}
	
}
