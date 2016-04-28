package com.bagri.xdm.api.test;

import static com.bagri.common.util.FileUtils.readTextFile;

import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import com.bagri.xdm.api.XDMDocumentManagement;
import com.bagri.xdm.api.XDMException;
import com.bagri.xdm.api.XDMModelManagement;
import com.bagri.xdm.api.XDMQueryManagement;
import com.bagri.xdm.api.XDMRepository;
import com.bagri.xdm.api.XDMTransactionManagement;
import com.bagri.xdm.common.XDMDocumentId;
import com.bagri.xdm.domain.XDMDocument;

public abstract class XDMManagementTest {

	protected static String sampleRoot;
	protected XDMRepository xRepo;
	protected Set<String> uris = new HashSet<>();
	
	protected String getFileName(String original) {
		return original;
	}
	
	protected String getUri(String fileName) {
		return Paths.get(fileName).getFileName().toString();
	}
	
	protected Properties getDocumentProperties() {
		return null;
	}
	
	protected XDMDocumentManagement getDocManagement() {
		return xRepo.getDocumentManagement();
	}
	
	protected XDMModelManagement getModelManagement() {
		return xRepo.getModelManagement();
	}

	protected XDMQueryManagement getQueryManagement() {
		return xRepo.getQueryManagement();
	}

	protected XDMTransactionManagement getTxManagement() {
		return xRepo.getTxManagement();
	}
	
	// TODO: think on how this can be generalized!
	// need to have some kind of XDMServerManagementTest!
	//protected void initRepo(ApplicationContext ctx) {
	//	xRepo = ctx.getBean(XDMRepository.class);
	//	RepositoryImpl xdmRepo = (RepositoryImpl) xRepo; 
	//	XDMSchema schema = xdmRepo.getSchema();
	//	if (schema == null) {
	//		schema = new XDMSchema(1, new java.util.Date(), "test", "test", "test schema", true, null);
	//		xdmRepo.setSchema(schema);
	//	}
	//}

	protected void removeDocumentsTest() throws Exception {
		if (getTxManagement().isInTransaction()) {
			getTxManagement().finishCurrentTransaction(true);
		}
		long txId =  getTxManagement().beginTransaction();
		for (String uri: uris) {
			getDocManagement().removeDocument(new XDMDocumentId(uri));
		}
		uris.clear();
		getTxManagement().commitTransaction(txId);
	}
	
	public XDMDocument createDocumentTest(String fileName) throws Exception {
		String xml = readTextFile(fileName);
		Properties props = getDocumentProperties();
		return getDocManagement().storeDocumentFromString(new XDMDocumentId(getUri(fileName)), xml, props);
	}
	
	public XDMDocument updateDocumentTest(String uri, String fileName) throws Exception {
		String xml = readTextFile(fileName);
		Properties props = getDocumentProperties();
		XDMDocumentId docId = new XDMDocumentId(uri);
		return getDocManagement().storeDocumentFromString(docId, xml, props);
	}

	public void removeDocumentTest(String uri) throws Exception {
		getDocManagement().removeDocument(new XDMDocumentId(uri));
	}

	public void storeSecurityTest() throws Exception {
		long txId = 0;
		try {
			txId = getTxManagement().beginTransaction();
		} catch (XDMException ex) {
			if (ex.getErrorCode() != XDMException.ecTransNoNested) {
				throw ex;
			}
		}
		uris.add(createDocumentTest(sampleRoot + getFileName("security1500.xml")).getUri());
		uris.add(createDocumentTest(sampleRoot + getFileName("security5621.xml")).getUri());
		uris.add(createDocumentTest(sampleRoot + getFileName("security9012.xml")).getUri());
		uris.add(createDocumentTest(sampleRoot + getFileName("security29674.xml")).getUri());
		if (txId > 0) {
			getTxManagement().commitTransaction(txId);
		}
	}
	
	public void storeOrderTest() throws Exception {
		long txId = 0;
		try {
			txId = getTxManagement().beginTransaction();
		} catch (XDMException ex) {
			if (ex.getErrorCode() != XDMException.ecTransNoNested) {
				throw ex;
			}
		}
		uris.add(createDocumentTest(sampleRoot + getFileName("order123.xml")).getUri());
		uris.add(createDocumentTest(sampleRoot + getFileName("order654.xml")).getUri());
		if (txId > 0) {
			getTxManagement().commitTransaction(txId);
		}
	}
	
	public void storeCustomerTest() throws Exception {
		long txId = 0;
		try {
			txId = getTxManagement().beginTransaction();
		} catch (XDMException ex) {
			if (ex.getErrorCode() != XDMException.ecTransNoNested) {
				throw ex;
			}
		}
		uris.add(createDocumentTest(sampleRoot + getFileName("custacc.xml")).getUri());
		if (txId > 0) {
			getTxManagement().commitTransaction(txId);
		}
	}

}
