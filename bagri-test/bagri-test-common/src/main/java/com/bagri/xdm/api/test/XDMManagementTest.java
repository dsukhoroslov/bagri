package com.bagri.xdm.api.test;

import static com.bagri.common.util.FileUtils.readTextFile;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.bagri.common.query.AxisType;
import com.bagri.common.query.Comparison;
import com.bagri.common.query.ExpressionContainer;
import com.bagri.common.query.PathBuilder;
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
	protected List<Long> ids = new ArrayList<Long>();
	
	protected String getFileName(String original) {
		return original;
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

	public Collection<String> getSecurity(String symbol) throws Exception {
		String prefix = getModelManagement().getNamespacePrefix("http://tpox-benchmark.com/security"); 
		int docType = getModelManagement().getDocumentType("/" + prefix + ":Security");
		PathBuilder path = new PathBuilder().
				addPathSegment(AxisType.CHILD, prefix, "Security").
				addPathSegment(AxisType.CHILD, prefix, "Symbol").
				addPathSegment(AxisType.CHILD, null, "text()");
		ExpressionContainer ec = new ExpressionContainer();
		ec.addExpression(docType, Comparison.EQ, path, "$sym", symbol);
		Map<String, String> params = new HashMap<String, String>();
		params.put(":sec", "/" + prefix + ":Security");
		return getQueryManagement().getXML(ec, ":sec", params);
	}
	
	protected void removeDocumentsTest() throws Exception {
		long txId =  getTxManagement().beginTransaction();
		for (Long key: ids) {
			getDocManagement().removeDocument(new XDMDocumentId(key));
		}
		ids.clear();
		getTxManagement().commitTransaction(txId);
	}
	
	public XDMDocument createDocumentTest(String fileName) throws Exception {
		String xml = readTextFile(fileName);
		return getDocManagement().storeDocumentFromString(null, xml, null);
	}
	
	public XDMDocument updateDocumentTest(long docKey, String uri, String fileName) throws Exception {
		String xml = readTextFile(fileName);
		return getDocManagement().storeDocumentFromString(new XDMDocumentId(docKey, uri), xml, null);
	}

	public void removeDocumentTest(long docKey) throws Exception {
		getDocManagement().removeDocument(new XDMDocumentId(docKey));
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
		ids.add(createDocumentTest(sampleRoot + getFileName("security1500.xml")).getDocumentKey());
		ids.add(createDocumentTest(sampleRoot + getFileName("security5621.xml")).getDocumentKey());
		ids.add(createDocumentTest(sampleRoot + getFileName("security9012.xml")).getDocumentKey());
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
		ids.add(createDocumentTest(sampleRoot + getFileName("order123.xml")).getDocumentKey());
		ids.add(createDocumentTest(sampleRoot + getFileName("order654.xml")).getDocumentKey());
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
		ids.add(createDocumentTest(sampleRoot + getFileName("custacc.xml")).getDocumentKey());
		if (txId > 0) {
			getTxManagement().commitTransaction(txId);
		}
	}

}
