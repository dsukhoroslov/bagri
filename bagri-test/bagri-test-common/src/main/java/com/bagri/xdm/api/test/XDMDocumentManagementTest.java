package com.bagri.xdm.api.test;

import static com.bagri.common.util.FileUtils.readTextFile;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.bagri.xdm.api.XDMException;
import com.bagri.xdm.common.XDMDocumentId;
import com.bagri.xdm.domain.XDMDocument;

public abstract class XDMDocumentManagementTest extends XDMManagementTest {

	@Test
	public void createSecurityTest() throws Exception {
		
		long txId = getTxManagement().beginTransaction();
		XDMDocument doc = createDocumentTest(sampleRoot + getFileName("security1500.xml"));
		assertNotNull(doc);
		assertEquals(doc.getTxStart(), txId);
		assertEquals(doc.getTxFinish(), 0);
		ids.add(doc.getDocumentKey());
		getTxManagement().commitTransaction(txId);
	}
	
	@Test
	public void updateSecurityTest() throws Exception {
		
		long txId = getTxManagement().beginTransaction();
		XDMDocument doc = createDocumentTest(sampleRoot + getFileName("security1500.xml"));
		assertNotNull(doc);
		assertEquals(doc.getTxStart(), txId);
		assertEquals(doc.getTxFinish(), 0);
		ids.add(doc.getDocumentKey());
		getTxManagement().commitTransaction(txId);
		long docId = doc.getDocumentId();
		int version = doc.getVersion();
		String uri = doc.getUri();
		
		txId = getTxManagement().beginTransaction();
		doc = updateDocumentTest(0, uri, sampleRoot + getFileName("security9012.xml"));
		assertNotNull(doc);
		assertEquals(doc.getTxStart(), txId);
		assertEquals(doc.getTxFinish(), 0);
		assertEquals(doc.getDocumentId(), docId);
		assertEquals(doc.getVersion(), ++version);
		assertEquals(doc.getUri(), uri);
		ids.add(doc.getDocumentKey());
		getTxManagement().commitTransaction(txId);

		txId = getTxManagement().beginTransaction();
		doc = updateDocumentTest(doc.getDocumentKey(), uri, sampleRoot + getFileName("security5621.xml"));
		assertNotNull(doc);
		assertEquals(doc.getTxStart(), txId);
		assertEquals(doc.getTxFinish(), 0);
		assertEquals(doc.getDocumentId(), docId);
		assertEquals(doc.getVersion(), ++version);
		assertEquals(doc.getUri(), uri);
		ids.add(doc.getDocumentKey());
		getTxManagement().commitTransaction(txId);
	}
	
	@Test
	public void removeSecurityTest() throws Exception {
		
		long txId = getTxManagement().beginTransaction();
		XDMDocument doc = createDocumentTest(sampleRoot + getFileName("security1500.xml"));
		assertNotNull(doc);
		assertEquals(doc.getTxStart(), txId);
		assertEquals(doc.getTxFinish(), 0);
		getTxManagement().commitTransaction(txId);
		long docId = doc.getDocumentId();
		long docKey = doc.getDocumentKey();
		ids.add(docKey);
		
		long txId2 = getTxManagement().beginTransaction();
		removeDocumentTest(docKey);
		doc = getDocManagement().getDocument(new XDMDocumentId(docKey));
		assertNotNull(doc);
		assertEquals(doc.getTxStart(), txId);
		assertEquals(doc.getTxFinish(), txId2);
		assertEquals(doc.getDocumentId(), docId);
		assertEquals(doc.getDocumentKey(), docKey);
		//assertTrue(doc.getVersion() == ++version);
		//assertEquals(doc.getUri(), uri);
		getTxManagement().commitTransaction(txId2);
		ids.add(doc.getDocumentKey());

		//doc = getDocManagement().getDocument(docKey);
		//assertNull(doc);
	}
	
	@Test
	public void selectSecurityTest() throws Exception {

		long txId = getTxManagement().beginTransaction();

		String fileName = sampleRoot + getFileName("security1500.xml");
		String xml = readTextFile(fileName);
		XDMDocument doc = createDocumentTest(fileName);
		assertNotNull(doc);
		assertEquals(doc.getTxStart(), txId);
		assertEquals(doc.getTxFinish(), 0);
		ids.add(doc.getDocumentKey());
		getTxManagement().commitTransaction(txId);

		String result = getDocManagement().getDocumentAsString(new XDMDocumentId(doc.getDocumentKey()));
		assertNotNull(result);
		assertEquals(result.length(), xml.length());
	}
	
}
