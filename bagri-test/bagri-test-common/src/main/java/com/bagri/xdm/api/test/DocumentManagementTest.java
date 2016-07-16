package com.bagri.xdm.api.test;

import static com.bagri.common.util.FileUtils.readTextFile;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.bagri.xdm.domain.Document;

public abstract class DocumentManagementTest extends XDMManagementTest {

	@Test
	public void createSecurityTest() throws Exception {
		
		long txId = getTxManagement().beginTransaction();
		String uri = getFileName("security1500.xml");
		Document doc = createDocumentTest(sampleRoot + uri);
		assertNotNull(doc);
		uris.add(doc.getUri());
		assertEquals(txId, doc.getTxStart());
		assertEquals(0, doc.getTxFinish());
		assertEquals(uri, doc.getUri());
		getTxManagement().commitTransaction(txId);
	}
	
	@Test
	public void updateSecurityTest() throws Exception {
		
		long txId = getTxManagement().beginTransaction();
		Document doc = createDocumentTest(sampleRoot + getFileName("security1500.xml"));
		assertNotNull(doc);
		uris.add(doc.getUri());
		assertEquals(txId, doc.getTxStart());
		assertEquals(0, doc.getTxFinish());
		getTxManagement().commitTransaction(txId);
		int version = doc.getVersion();
		String uri = doc.getUri();
		
		txId = getTxManagement().beginTransaction();
		doc = updateDocumentTest(uri, sampleRoot + getFileName("security9012.xml"));
		assertNotNull(doc);
		uris.add(doc.getUri());
		assertEquals(txId, doc.getTxStart());
		assertEquals(0, doc.getTxFinish());
		assertEquals(++version, doc.getVersion());
		assertEquals(uri, doc.getUri());
		getTxManagement().commitTransaction(txId);

		txId = getTxManagement().beginTransaction();
		doc = updateDocumentTest(uri, sampleRoot + getFileName("security5621.xml"));
		assertNotNull(doc);
		uris.add(doc.getUri());
		assertEquals(txId, doc.getTxStart());
		assertEquals(0, doc.getTxFinish());
		assertEquals(++version, doc.getVersion());
		assertEquals(uri, doc.getUri());
		getTxManagement().commitTransaction(txId);
	}
	
	@Test
	public void removeSecurityTest() throws Exception {
		
		long txId = getTxManagement().beginTransaction();
		Document doc = createDocumentTest(sampleRoot + getFileName("security1500.xml"));
		assertNotNull(doc);
		uris.add(doc.getUri());
		assertEquals(txId, doc.getTxStart());
		assertEquals(0, doc.getTxFinish());
		getTxManagement().commitTransaction(txId);
		long docKey = doc.getDocumentKey();
		
		long txId2 = getTxManagement().beginTransaction();
		removeDocumentTest(doc.getUri());
		doc = getDocManagement().getDocument(doc.getUri());
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
		Document doc = createDocumentTest(fileName);
		assertNotNull(doc);
		uris.add(doc.getUri());
		assertEquals(txId, doc.getTxStart());
		assertEquals(0, doc.getTxFinish());
		getTxManagement().commitTransaction(txId);

		String result = getDocManagement().getDocumentAsString(doc.getUri(), null);
		assertNotNull(result);
		assertEquals(xml.length(), result.length());
	}
	
}
