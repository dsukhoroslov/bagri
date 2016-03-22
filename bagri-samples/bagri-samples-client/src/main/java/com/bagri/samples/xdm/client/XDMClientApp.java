package com.bagri.samples.xdm.client;

import static com.bagri.xdm.common.XDMConstants.pn_client_dataFactory;
import static com.bagri.xdm.common.XDMConstants.pn_schema_address;
import static com.bagri.xdm.common.XDMConstants.pn_schema_name;
import static com.bagri.xdm.common.XDMConstants.pn_schema_password;
import static com.bagri.xdm.common.XDMConstants.pn_schema_user;

import java.util.Iterator;
import java.util.Properties;

import javax.xml.xquery.XQException;
import javax.xml.xquery.XQExpression;
import javax.xml.xquery.XQResultSequence;

import com.bagri.samples.client.BagriClientApp;
import com.bagri.xdm.api.XDMException;
import com.bagri.xdm.api.XDMRepository;
import com.bagri.xdm.client.hazelcast.impl.RepositoryImpl;
import com.bagri.xdm.common.XDMDocumentId;
import com.bagri.xdm.domain.XDMDocument;
import com.bagri.xqj.BagriXQDataFactory;
import com.bagri.xquery.api.XQProcessor;
import com.bagri.xquery.saxon.XQProcessorClient;

public class XDMClientApp implements BagriClientApp {
	
	private XQProcessor proc;
	private XDMRepository xRepo;
	
	public static void main(String[] args) throws Exception {
		
		if (args.length < 4) {
			throw new XDMException("wrong number of arguments passed. Expected: schemaAddress schemaName userName password", 0);
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
		xRepo = new RepositoryImpl(props);
	}
	
	public XDMClientApp(XDMRepository xRepo) {
		this.xRepo = xRepo;
	}
	
	@Override
	public void close() { 
		xRepo.close();
	}
	
	@Override
	public boolean createDocument(String uri, String content) throws XDMException {
		
		return storeDocument(uri, content) > 0;
	}
	
	@Override
	public String readDocument(String uri) throws XDMException {
		
		XDMDocumentId docId = new XDMDocumentId(uri);
		return xRepo.getDocumentManagement().getDocumentAsString(docId);
	}
	
	@Override
	public String queryDocumentByUri(String uri) throws XDMException, XQException {

		String query = "for $doc in fn:doc(\"" + uri + "\")\n" +
				"return $doc\n";

		Iterator itr = xRepo.getQueryManagement().executeQuery(query, null, new Properties());
	    String result = null;
	    if (itr.hasNext()) {
			result = proc.convertToString(itr.next(), null);
	    }
	    return result;
	}
	
	@Override
	public String queryDocumentFromCollection() throws XDMException, XQException {

		String query = "for $doc in fn:collection()\n" +
				"return $doc\n";

		Iterator itr = xRepo.getQueryManagement().executeQuery(query, null, new Properties());
	    String result = null;
	    if (itr.hasNext()) {
			result = proc.convertToString(itr.next(), null);
	    }
	    return result;
	}
	
	@Override
	public boolean updateDocument(String uri, String content) throws XDMException {
		
		return storeDocument(uri, content) > 1;
	}
	
	@Override
	public void deleteDocument(String uri) throws XDMException {

		XDMDocumentId docId = new XDMDocumentId(uri);
		xRepo.getDocumentManagement().removeDocument(docId);
	}
	
	private long storeDocument(String uri, String content) throws XDMException {
		
		Properties props = new Properties();
		XDMDocumentId docId = new XDMDocumentId(uri);
		XDMDocument xDoc = xRepo.getDocumentManagement().storeDocumentFromString(docId, content, props);
		return xDoc.getDocumentKey();
	} 

}
