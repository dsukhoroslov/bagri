package com.bagri.xdm.api.test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.bagri.xdm.domain.XDMDocument;

public abstract class XDMDocumentManagementTest extends XDMManagementTest {

	@Test
	public void createSecurityTest() throws IOException {
		
		long txId = getTxManagement().beginTransaction();
		XDMDocument doc = createDocumentTest(sampleRoot + "security1500.xml");
		//Assert.assertTrue(doc.getDocumentId() == 1);
		Assert.assertTrue(doc.getTxStart() == txId);
		Assert.assertTrue(doc.getTxFinish() == 0);
		ids.add(doc.getDocumentKey());
		//ids.add(createDocumentTest(sampleRoot + "security5621.xml").getDocumentKey());
		//ids.add(createDocumentTest(sampleRoot + "security9012.xml").getDocumentKey());
		getTxManagement().commitTransaction(txId);
	}
	
	@Test
	public void updateSecurityTest() throws IOException {
		
		long txId = getTxManagement().beginTransaction();
		XDMDocument doc = createDocumentTest(sampleRoot + "security1500.xml");
		//Assert.assertTrue(doc.getDocumentId() == 1);
		Assert.assertTrue(doc.getTxStart() == txId);
		Assert.assertTrue(doc.getTxFinish() == 0);
		ids.add(doc.getDocumentKey());
		getTxManagement().commitTransaction(txId);
		long docId = doc.getDocumentId();
		int version = doc.getVersion();
		String uri = doc.getUri();
		
		txId = getTxManagement().beginTransaction();
		doc = updateDocumentTest(0, uri, sampleRoot + "security9012.xml");
		Assert.assertTrue(doc.getTxStart() == txId);
		Assert.assertTrue(doc.getTxFinish() == 0);
		Assert.assertTrue(doc.getDocumentId() == docId);
		Assert.assertTrue(doc.getVersion() == ++version);
		Assert.assertEquals(doc.getUri(), uri);
		ids.add(doc.getDocumentKey());
		getTxManagement().commitTransaction(txId);

		txId = getTxManagement().beginTransaction();
		doc = updateDocumentTest(doc.getDocumentKey(), null, sampleRoot + "security5621.xml");
		Assert.assertTrue(doc.getTxStart() == txId);
		Assert.assertTrue(doc.getTxFinish() == 0);
		Assert.assertTrue(doc.getDocumentId() == docId);
		Assert.assertTrue(doc.getVersion() == ++version);
		//Assert.assertEquals(doc.getUri(), uri);
		ids.add(doc.getDocumentKey());
		getTxManagement().commitTransaction(txId);
	}
	
}
