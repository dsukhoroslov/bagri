package com.bagri.xdm.api.test;

import static com.bagri.common.util.FileUtils.readTextFile;

import java.io.IOException;
import java.util.Collection;

import org.junit.Assert;
import org.junit.Test;

import com.bagri.xdm.api.XDMDocumentManagement;
import com.bagri.xdm.api.XDMSchemaDictionary;
import com.bagri.xdm.domain.XDMPath;

public abstract class XDMSchemaDictionaryTest {

	protected static String sampleRoot;
	protected XDMDocumentManagement dMgr;
	protected XDMSchemaDictionary mDictionary;
	
	public void registerSecuritySchemaTest() throws IOException { 
		//String schema = sampleRoot + "security.xsd";
		String schema = readTextFile(sampleRoot + "security.xsd");
		mDictionary.registerSchema(schema);
	}
	
	public void registerCustaccSchemaTest() throws IOException { 
		//String schema = sampleRoot + "custacc.xsd";
		String schema = readTextFile(sampleRoot + "custacc.xsd");
		mDictionary.registerSchema(schema);
	}
	
	public void registerCommonSchemaTest() throws IOException { 
		//String schema = sampleRoot + "custacc.xsd";
		String schema = readTextFile(sampleRoot + "Common.xsd");
		mDictionary.registerSchema(schema);
	}
	
	public Collection<XDMPath> getSecurityPath() {
		String prefix = mDictionary.getNamespacePrefix("http://tpox-benchmark.com/security"); 
		int docType = mDictionary.getDocumentType("/" + prefix + ":Security");
		return mDictionary.getTypePaths(docType);
	}

	public Collection<XDMPath> getCustomerPath() {
		String prefix = mDictionary.getNamespacePrefix("http://tpox-benchmark.com/custacc"); 
		int docType = mDictionary.getDocumentType("/" + prefix + ":Customer");
		return mDictionary.getTypePaths(docType);
	}

	@Test
	public void getSecurityPathTest() {
		Collection<XDMPath> sec = getSecurityPath();
		Assert.assertNotNull(sec);
		Assert.assertTrue(sec.size() > 0);
	}

	@Test
	public void getCustomerPathTest() {
		Collection<XDMPath> sec = getCustomerPath();
		Assert.assertNotNull(sec);
		Assert.assertTrue(sec.size() > 0);
	}

}
