package com.bagri.xdm.api.test;

import static com.bagri.common.util.FileUtils.readTextFile;

import java.io.IOException;
import java.util.Collection;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import com.bagri.xdm.api.DocumentManagement;
import com.bagri.xdm.api.ModelManagement;
import com.bagri.xdm.api.SchemaRepository;
import com.bagri.xdm.domain.Path;

public abstract class XDMModelManagementTest extends XDMManagementTest {

	//protected XDMModelManagement mDictionary;
	
	public void registerSecuritySchemaTest() throws Exception { 
		//String schema = sampleRoot + "security.xsd";
		String schema = readTextFile(sampleRoot + "security.xsd");
		getModelManagement().registerSchema(schema);
	}
	
	public void registerCustaccSchemaTest() throws Exception { 
		//String schema = sampleRoot + "custacc.xsd";
		String schema = readTextFile(sampleRoot + "custacc.xsd");
		getModelManagement().registerSchema(schema);
	}
	
	//public void registerCommonSchemaTest() throws IOException { 
		//String schema = sampleRoot + "custacc.xsd";
	//	String schema = readTextFile(sampleRoot + "Common.xsd");
	//	mDictionary.registerSchema(schema);
	//}
	
	protected Collection<Path> getPath(String namespace, String template) {
		String prefix = getModelManagement().getNamespacePrefix(namespace);
		String path = String.format(template, prefix);
		int docType = getModelManagement().getDocumentType(path);
		return getModelManagement().getTypePaths(docType);
	}
	
	public Collection<Path> getSecurityPath() {
		return getPath("http://tpox-benchmark.com/security", "/%s:Security");
	}
	
	public Collection<Path> getCustomerPath() {
		return getPath("http://tpox-benchmark.com/custacc", "/%s:Customer");
	}

	@Test
	public void registerSecurityPathTest() throws Exception {
		registerSecuritySchemaTest();
		Collection<Path> sec = getSecurityPath();
		Assert.assertNotNull(sec);
		Assert.assertTrue(sec.size() > 0);
	}

	@Test
	public void registerCustomerPathTest() throws Exception {
		registerCustaccSchemaTest();
		Collection<Path> sec = getCustomerPath();
		Assert.assertNotNull(sec);
		Assert.assertTrue(sec.size() > 0);
	}

	@Test
	//@Ignore
	public void getSecurityPathTest() throws Exception {
		storeSecurityTest();
		Collection<Path> sec = getSecurityPath();
		Assert.assertNotNull(sec);
		Assert.assertTrue(sec.size() > 0);
	}

	@Test
	//@Ignore
	public void getCustomerPathTest() throws Exception {
		storeCustomerTest();
		Collection<Path> sec = getCustomerPath();
		Assert.assertNotNull(sec);
		Assert.assertTrue(sec.size() > 0);
	}
	
}
