package com.bagri.core.test;

import static com.bagri.core.Constants.pn_document_headers;
import static com.bagri.support.util.FileUtils.readTextFile;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Properties;

import org.junit.Test;

import com.bagri.core.api.DocumentAccessor;

public abstract class DocumentManagementTest extends BagriManagementTest {

	@Test
	public void createSecurityTest() throws Exception {
		
		long txId = getTxManagement().beginTransaction();
		String uri = getFileName("security1500.xml");
		DocumentAccessor doc = createDocumentTest(sampleRoot + uri);
		assertNotNull(doc);
		uris.add(doc.getUri());
		assertEquals(txId, doc.getTxStart());
		assertEquals(0L, doc.getTxFinish());
		assertEquals(uri, doc.getUri());
		getTxManagement().commitTransaction(txId);
	}
	
	@Test
	public void updateSecurityTest() throws Exception {
		
		long txId = getTxManagement().beginTransaction();
		DocumentAccessor doc = createDocumentTest(sampleRoot + getFileName("security1500.xml"));
		assertNotNull(doc);
		uris.add(doc.getUri());
		assertEquals(txId, doc.getTxStart());
		assertEquals(0L, doc.getTxFinish());
		getTxManagement().commitTransaction(txId);
		int version = doc.getVersion();
		String uri = doc.getUri();
		
		txId = getTxManagement().beginTransaction();
		doc = updateDocumentTest(uri, sampleRoot + getFileName("security9012.xml"));
		assertNotNull(doc);
		uris.add(doc.getUri());
		assertEquals(txId, doc.getTxStart());
		assertEquals(0L, doc.getTxFinish());
		assertEquals(++version, doc.getVersion());
		assertEquals(uri, doc.getUri());
		getTxManagement().commitTransaction(txId);

		txId = getTxManagement().beginTransaction();
		doc = updateDocumentTest(uri, sampleRoot + getFileName("security5621.xml"));
		assertNotNull(doc);
		uris.add(doc.getUri());
		assertEquals(txId, doc.getTxStart());
		assertEquals(0L, doc.getTxFinish());
		assertEquals(++version, doc.getVersion());
		assertEquals(uri, doc.getUri());
		getTxManagement().commitTransaction(txId);
	}
	
	@Test
	public void removeSecurityTest() throws Exception {
		
		long txId = getTxManagement().beginTransaction();
		DocumentAccessor doc = createDocumentTest(sampleRoot + getFileName("security1500.xml"));
		assertNotNull(doc);
		uris.add(doc.getUri());
		assertEquals(txId, doc.getTxStart());
		assertEquals(0L, doc.getTxFinish());
		getTxManagement().commitTransaction(txId);
		long docKey = doc.getDocumentKey();
		
		long txId2 = getTxManagement().beginTransaction();
		removeDocumentTest(doc.getUri());
		doc = getDocManagement().getDocument(doc.getUri(), getDocumentProperties());
		// now it is null.. think about it - is it correct or not?
		//assertNotNull(doc);
		//assertEquals(txId, doc.getTxStart());
		//assertEquals(txId2, doc.getTxFinish());
		//assertEquals(docId, doc.getDocumentId());
		//assertEquals(docKey, doc.getDocumentKey());
		//assertTrue(doc.getVersion() == ++version);
		//assertEquals(doc.getUri(), uri);
		getTxManagement().commitTransaction(txId2);
		//uris.add(doc.getUri());

		//doc = getDocManagement().getDocument(docKey);
		//assertNull(doc);
	}
	
	@Test
	public void selectSecurityTest() throws Exception {

		long txId = getTxManagement().beginTransaction();

		String fileName = sampleRoot + getFileName("security1500.xml");
		String xml = readTextFile(fileName);
		DocumentAccessor doc = createDocumentTest(fileName);
		assertNotNull(doc);
		uris.add(doc.getUri());
		assertEquals(txId, doc.getTxStart());
		assertEquals(0L, doc.getTxFinish());
		getTxManagement().commitTransaction(txId);

		Properties props = getDocumentProperties();
		props.setProperty(pn_document_headers, String.valueOf(DocumentAccessor.HDR_CONTENT));
		doc = getDocManagement().getDocument(doc.getUri(), props);
		assertNotNull(doc);
		assertNotNull(doc.getContent());
		String content = doc.getContent();
		assertEquals(xml.length(), content.length());
	}
	
}
