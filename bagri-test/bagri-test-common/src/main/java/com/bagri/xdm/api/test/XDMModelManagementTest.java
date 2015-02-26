package com.bagri.xdm.api.test;

import static com.bagri.common.util.FileUtils.readTextFile;

import java.io.IOException;
import java.util.Collection;

import org.junit.Assert;
import org.junit.Test;

import com.bagri.xdm.api.XDMDocumentManagement;
import com.bagri.xdm.api.XDMModelManagement;
import com.bagri.xdm.api.XDMRepository;
import com.bagri.xdm.domain.XDMPath;

public abstract class XDMModelManagementTest extends XDMManagementTest {

	//protected XDMModelManagement mDictionary;
	
	public void registerSecuritySchemaTest() throws IOException { 
		//String schema = sampleRoot + "security.xsd";
		String schema = readTextFile(sampleRoot + "security.xsd");
		getModelManagement().registerSchema(schema);
	}
	
	public void registerCustaccSchemaTest() throws IOException { 
		//String schema = sampleRoot + "custacc.xsd";
		String schema = readTextFile(sampleRoot + "custacc.xsd");
		getModelManagement().registerSchema(schema);
	}
	
	//public void registerCommonSchemaTest() throws IOException { 
		//String schema = sampleRoot + "custacc.xsd";
	//	String schema = readTextFile(sampleRoot + "Common.xsd");
	//	mDictionary.registerSchema(schema);
	//}
	
	public Collection<XDMPath> getSecurityPath() {
		String prefix = getModelManagement().getNamespacePrefix("http://tpox-benchmark.com/security"); 
		int docType = getModelManagement().getDocumentType("/" + prefix + ":Security");
		return getModelManagement().getTypePaths(docType);
	}
	
	public Collection<XDMPath> getCustomerPath() {
		String prefix = getModelManagement().getNamespacePrefix("http://tpox-benchmark.com/custacc"); 
		int docType = getModelManagement().getDocumentType("/" + prefix + ":Customer");
		return getModelManagement().getTypePaths(docType);
	}

	@Test
	public void registerSecurityPathTest() throws IOException {
		registerSecuritySchemaTest();
		Collection<XDMPath> sec = getSecurityPath();
		Assert.assertNotNull(sec);
		Assert.assertTrue(sec.size() > 0);
	}

	@Test
	public void registerCustomerPathTest() throws IOException {
		registerCustaccSchemaTest();
		Collection<XDMPath> sec = getCustomerPath();
		Assert.assertNotNull(sec);
		Assert.assertTrue(sec.size() > 0);
	}

	@Test
	public void getSecurityPathTest() throws IOException {
		storeSecurityTest();
		Collection<XDMPath> sec = getSecurityPath();
		Assert.assertNotNull(sec);
		Assert.assertTrue(sec.size() > 0);
	}

	@Test
	public void getCustomerPathTest() throws IOException {
		storeCustomerTest();
		Collection<XDMPath> sec = getCustomerPath();
		Assert.assertNotNull(sec);
		Assert.assertTrue(sec.size() > 0);
	}
	
}
