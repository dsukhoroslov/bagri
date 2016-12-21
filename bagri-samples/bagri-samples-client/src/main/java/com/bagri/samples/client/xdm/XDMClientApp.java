package com.bagri.samples.client.xdm;

import static com.bagri.core.Constants.pn_client_dataFactory;
import static com.bagri.core.Constants.pn_schema_address;
import static com.bagri.core.Constants.pn_schema_name;
import static com.bagri.core.Constants.pn_schema_password;
import static com.bagri.core.Constants.pn_schema_user;

import java.util.Iterator;
import java.util.Properties;

import javax.xml.xquery.XQException;

import com.bagri.client.hazelcast.impl.SchemaRepositoryImpl;
import com.bagri.core.api.ResultCursor;
import com.bagri.core.api.SchemaRepository;
import com.bagri.core.api.BagriException;
import com.bagri.core.model.Document;
import com.bagri.core.xquery.api.XQProcessor;
import com.bagri.samples.client.BagriClientApp;
import com.bagri.xqj.BagriXQDataFactory;
import com.bagri.xquery.saxon.XQProcessorClient;

public class XDMClientApp implements BagriClientApp {
	
	private XQProcessor proc;
	private SchemaRepository xRepo;
	
	public static void main(String[] args) throws Exception {
		
		if (args.length < 4) {
			throw new BagriException("wrong number of arguments passed. Expected: schemaAddress schemaName userName password", 0);
		}
		
		Properties props = new Properties();
	    props.setProperty(pn_schema_address, args[0]);
	    props.setProperty(pn_schema_name, args[1]);
	    props.setProperty(pn_schema_user, args[2]);
	    props.setProperty(pn_schema_password, args[3]);
	    
		XDMClientApp client = new XDMClientApp(props);
		tester.testClient(client);
	}
	
	public XDMClientApp(Properties props) {
		proc = new XQProcessorClient();
		BagriXQDataFactory xqFactory = new BagriXQDataFactory();
		xqFactory.setProcessor(proc);
		props.put(pn_client_dataFactory,  xqFactory);
		xRepo = new SchemaRepositoryImpl(props);
	}
	
	public XDMClientApp(SchemaRepository xRepo) {
		this.xRepo = xRepo;
	}
	
	@Override
	public void close() { 
		xRepo.close();
	}
	
	@Override
	public boolean createDocument(String uri, String content) throws BagriException {
		
		return storeDocument(uri, content) > 0;
	}
	
	@Override
	public String readDocument(String uri) throws BagriException {
		
		return xRepo.getDocumentManagement().getDocumentAsString(uri, null);
	}
	
	@Override
	public String queryDocumentByUri(String uri) throws Exception {

		String query = "for $doc in fn:doc(\"" + uri + "\")\n" +
				"return $doc\n";

		try (ResultCursor cursor = xRepo.getQueryManagement().executeQuery(query, null, new Properties())) {
			String result = null;
			if (cursor.next()) {
				result = cursor.getString(); // proc.convertToString(itr.next(), null);
			}
			return result;
		}
	}
	
	@Override
	public String queryDocumentFromCollection() throws Exception {

		String query = "for $doc in fn:collection()\n" +
				"return $doc\n";

		try (ResultCursor cursor = xRepo.getQueryManagement().executeQuery(query, null, new Properties())) {
			String result = null;
			if (cursor.next()) {
				result = cursor.getString(); // proc.convertToString(itr.next(), null);
			}
			return result;
		}
	}
	
	@Override
	public boolean updateDocument(String uri, String content) throws BagriException {
		
		return storeDocument(uri, content) > 1;
	}
	
	@Override
	public void deleteDocument(String uri) throws BagriException {

		xRepo.getDocumentManagement().removeDocument(uri);
	}
	
	private int storeDocument(String uri, String content) throws BagriException {
		
		Properties props = new Properties();
		Document xDoc = xRepo.getDocumentManagement().storeDocumentFromString(uri, content, props);
		return xDoc.getVersion();
	} 

}
