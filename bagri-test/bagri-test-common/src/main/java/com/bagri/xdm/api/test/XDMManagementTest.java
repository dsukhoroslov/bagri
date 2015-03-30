package com.bagri.xdm.api.test;

import static com.bagri.common.util.FileUtils.readTextFile;

import java.io.IOException;
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
import com.bagri.xdm.api.XDMModelManagement;
import com.bagri.xdm.api.XDMQueryManagement;
import com.bagri.xdm.api.XDMRepository;
import com.bagri.xdm.api.XDMTransactionManagement;

public abstract class XDMManagementTest {

	protected static String sampleRoot;
	protected XDMRepository xRepo;
	protected List<Long> ids = new ArrayList<Long>();
	
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

	public Collection<String> getSecurity(String symbol) {
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
	
	protected void removeDocumentsTest() { 
		long txId =  getTxManagement().beginTransaction();
		for (Long id: ids) {
			getDocManagement().removeDocument(id);
		}
		ids.clear();
		getTxManagement().commitTransaction(txId);
	}
	
	public void storeSecurityTest() throws IOException {
		long txId =  getTxManagement().beginTransaction();
		String path = sampleRoot + "security1500.xml"; 
		String xml = readTextFile(path);
		ids.add(getDocManagement().storeDocumentFromString(0, null, xml).getDocumentKey());

		path = sampleRoot + "security5621.xml";
		xml = readTextFile(path);
		ids.add(getDocManagement().storeDocumentFromString(0, null, xml).getDocumentKey());

		path = sampleRoot + "security9012.xml";
		xml = readTextFile(path);
		ids.add(getDocManagement().storeDocumentFromString(0, null, xml).getDocumentKey());
		getTxManagement().commitTransaction(txId);
	}
	
	public void storeOrderTest() throws IOException {
		long txId =  getTxManagement().beginTransaction();
		String xml = readTextFile(sampleRoot + "order123.xml");
		ids.add(getDocManagement().storeDocumentFromString(0, null, xml).getDocumentKey());
		xml = readTextFile(sampleRoot + "order654.xml");
		ids.add(getDocManagement().storeDocumentFromString(0, null, xml).getDocumentKey());
		getTxManagement().commitTransaction(txId);
	}
	
	public void storeCustomerTest() throws IOException {
		long txId =  getTxManagement().beginTransaction();
		String xml = readTextFile(sampleRoot + "custacc.xml");
		ids.add(getDocManagement().storeDocumentFromString(0, null, xml).getDocumentKey());
		getTxManagement().commitTransaction(txId);
	}
	

}
