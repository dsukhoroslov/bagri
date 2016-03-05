package com.bagri.samples.xdm.client;

import static com.bagri.xdm.common.XDMConstants.pn_client_dataFactory;
import static com.bagri.xdm.common.XDMConstants.pn_schema_address;
import static com.bagri.xdm.common.XDMConstants.pn_schema_name;
import static com.bagri.xdm.common.XDMConstants.pn_schema_password;
import static com.bagri.xdm.common.XDMConstants.pn_schema_user;
import static com.bagri.xqj.BagriXQDataSource.ADDRESS;
import static com.bagri.xqj.BagriXQDataSource.PASSWORD;
import static com.bagri.xqj.BagriXQDataSource.SCHEMA;
import static com.bagri.xqj.BagriXQDataSource.USER;

import java.util.Properties;

import com.bagri.common.util.FileUtils;
import com.bagri.xdm.api.XDMException;
import com.bagri.xdm.api.XDMRepository;
import com.bagri.xdm.client.hazelcast.impl.RepositoryImpl;
import com.bagri.xdm.common.XDMDocumentId;
import com.bagri.xdm.domain.XDMDocument;
import com.bagri.xqj.BagriXQDataFactory;
import com.bagri.xquery.api.XQProcessor;
import com.bagri.xquery.saxon.XQProcessorClient;

public class XDMClientApp {
	
	private XDMRepository xRepo;
	
	public static void main(String[] args) throws XDMException {
		
		if (args.length < 4) {
			throw new XDMException("wrong number of arguments passed. Expected: schemaAddress schemaName userName password", 0);
		}
		
		Properties props = new Properties();
	    props.setProperty(pn_schema_address, args[0]);
	    props.setProperty(pn_schema_name, args[1]);
	    props.setProperty(pn_schema_user, args[2]);
	    props.setProperty(pn_schema_password, args[3]);
		XQProcessor proc = new XQProcessorClient();
		BagriXQDataFactory xqFactory = new BagriXQDataFactory();
		xqFactory.setProcessor(proc);
		props.put(pn_client_dataFactory,  xqFactory);
		XDMClientApp client = new XDMClientApp(props); 
		
		String uri = "test_document";
		try {
			String xml = "<content>XML Content</content>";
			if (!client.createDocument(uri, xml)) {
				System.out.println("ERROR: document was not created");
				return;
			}
			xml = "<content>Updated XML Content</content>";
			if (!client.updateDocument(uri, xml)) {
				System.out.println("ERROR: document was not updated");
				return;
			}
			xml = client.readDocument(uri);
			if (xml != null) {
				System.out.println("got document: " + xml);
			} else {
				System.out.println("ERROR: document was not read");
				return;
			}
			client.deleteDocument(uri);
			xml = client.readDocument(uri);
			if (xml != null) {
				System.out.println("ERROR: document still exists: " + xml);
			}
		} finally {
			client.xRepo.close();
		}
	}
	
	public XDMClientApp(Properties props) {
		
		xRepo = new RepositoryImpl(props);
	}
	
	public boolean createDocument(String uri, String content) throws XDMException {
		
		Properties props = new Properties();
		XDMDocumentId docId = new XDMDocumentId(uri);
		XDMDocument xDoc = xRepo.getDocumentManagement().storeDocumentFromString(docId, content, props);
		return xDoc != null;
	}
	
	public String readDocument(String uri) throws XDMException {
		
		XDMDocumentId docId = new XDMDocumentId(uri);
		return xRepo.getDocumentManagement().getDocumentAsString(docId);
	}
	
	public boolean updateDocument(String uri, String content) throws XDMException {
		
		Properties props = new Properties();
		XDMDocumentId docId = new XDMDocumentId(uri);
		XDMDocument xDoc = xRepo.getDocumentManagement().storeDocumentFromString(docId, content, props);
		return xDoc != null;
	}
	
	public void deleteDocument(String uri) throws XDMException {

		XDMDocumentId docId = new XDMDocumentId(uri);
		xRepo.getDocumentManagement().removeDocument(docId);
	}

}
