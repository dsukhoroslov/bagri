package com.bagri.core.test;

import static com.bagri.support.util.FileUtils.readTextFile;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.bagri.core.api.DocumentAccessor;
import com.bagri.core.model.Document;

public abstract class DocumentManagementTest extends BagriManagementTest {

	@Test
	public void createSecurityTest() throws Exception {
		
		long txId = getTxManagement().beginTransaction();
		String uri = getFileName("security1500.xml");
		DocumentAccessor doc = createDocumentTest(sampleRoot + uri);
		assertNotNull(doc);
		uris.add(doc.getUri());
		assertEquals(txId, doc.getHeader(DocumentAccessor.HDR_TX_START));
		assertEquals(0L, doc.getHeader(DocumentAccessor.HDR_TX_FINISH));
		assertEquals(uri, doc.getUri());
		getTxManagement().commitTransaction(txId);
	}
	
	@Test
	public void updateSecurityTest() throws Exception {
		
		long txId = getTxManagement().beginTransaction();
		DocumentAccessor doc = createDocumentTest(sampleRoot + getFileName("security1500.xml"));
		assertNotNull(doc);
		uris.add(doc.getUri());
		assertEquals(txId, doc.getHeader(DocumentAccessor.HDR_TX_START));
		assertEquals(0L, doc.getHeader(DocumentAccessor.HDR_TX_FINISH));
		getTxManagement().commitTransaction(txId);
		int version = doc.getVersion();
		String uri = doc.getUri();
		
		txId = getTxManagement().beginTransaction();
		doc = updateDocumentTest(uri, sampleRoot + getFileName("security9012.xml"));
		assertNotNull(doc);
		uris.add(doc.getUri());
		assertEquals(txId, doc.getHeader(DocumentAccessor.HDR_TX_START));
		assertEquals(0L, doc.getHeader(DocumentAccessor.HDR_TX_FINISH));
		assertEquals(++version, doc.getVersion());
		assertEquals(uri, doc.getUri());
		getTxManagement().commitTransaction(txId);

		txId = getTxManagement().beginTransaction();
		doc = updateDocumentTest(uri, sampleRoot + getFileName("security5621.xml"));
		assertNotNull(doc);
		uris.add(doc.getUri());
		assertEquals(txId, doc.getHeader(DocumentAccessor.HDR_TX_START));
		assertEquals(0L, doc.getHeader(DocumentAccessor.HDR_TX_FINISH));
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
		assertEquals(txId, doc.getHeader(DocumentAccessor.HDR_TX_START));
		assertEquals(0L, doc.getHeader(DocumentAccessor.HDR_TX_FINISH));
		getTxManagement().commitTransaction(txId);
		long docKey = doc.getHeader(DocumentAccessor.HDR_KEY);
		
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
		assertEquals(txId, doc.getHeader(DocumentAccessor.HDR_TX_START));
		assertEquals(0L, doc.getHeader(DocumentAccessor.HDR_TX_FINISH));
		getTxManagement().commitTransaction(txId);

		doc = getDocManagement().getDocument(doc.getUri(), getDocumentProperties());
		assertNotNull(doc);
		assertNotNull(doc.getContent());
		String content = doc.getContent();
		assertEquals(xml.length(), content.length());
	}
	
}
