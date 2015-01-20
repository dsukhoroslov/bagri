package com.bagri.xdm.api.test;

import static com.bagri.common.util.FileUtils.readTextFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import com.bagri.common.query.AxisType;
import com.bagri.common.query.Comparison;
//import com.bagri.common.query.ExpressionBuilder;
import com.bagri.common.query.ExpressionContainer;
import com.bagri.common.query.PathBuilder;
import com.bagri.xdm.access.api.XDMDocumentManagement;
import com.bagri.xdm.access.api.XDMSchemaDictionary;
import com.bagri.xdm.domain.XDMDocument;
import com.bagri.xdm.domain.XDMPath;

public abstract class XDMDocumentManagementTest {

	protected static String sampleRoot;
	protected XDMDocumentManagement dMgr;
	protected XDMSchemaDictionary mDictionary;
	protected List<Long> ids = new ArrayList<Long>();
	
	public void storeSecurityTest() throws IOException {
		String path = sampleRoot + "security1500.xml"; 
		String xml = readTextFile(path);
		ids.add(dMgr.storeDocument(xml).getDocumentId());

		path = sampleRoot + "security5621.xml";
		xml = readTextFile(path);
		ids.add(dMgr.storeDocument(xml).getDocumentId());

		path = sampleRoot + "security9012.xml";
		xml = readTextFile(path);
		ids.add(dMgr.storeDocument(xml).getDocumentId());

		//String prefix = mDictionary.getNamespacePrefix("http://tpox-benchmark.com/security"); 
		//int docType = mDictionary.getDocumentType("/" + prefix + ":Security");
		//mDictionary.getPathFromRegex(docType, ".*");
	}
	
	public void storeOrderTest() throws IOException {
		String xml = readTextFile(sampleRoot + "order123.xml");
		XDMDocument doc = dMgr.storeDocument(xml);
		ids.add(doc.getDocumentId());
		xml = readTextFile(sampleRoot + "order654.xml");
		ids.add(dMgr.storeDocument(xml).getDocumentId());
	}
	
	public void storeCustomerTest() throws IOException {
		String xml = readTextFile(sampleRoot + "custacc.xml");
		ids.add(dMgr.storeDocument(xml).getDocumentId());
	}
	
	public void removeDocumentsTest() { 
		for (Long id: ids) {
			dMgr.removeDocument(id);
		}
		ids.clear();
	}
	
	public Collection<String> getPrice(String symbol) {
		String prefix = mDictionary.getNamespacePrefix("http://tpox-benchmark.com/security"); 
		int docType = mDictionary.getDocumentType("/" + prefix + ":Security");
		PathBuilder path = new PathBuilder().
				addPathSegment(AxisType.CHILD, prefix, "Security").
				addPathSegment(AxisType.CHILD, prefix, "Symbol").
				addPathSegment(AxisType.CHILD, null, "text()");
		ExpressionContainer ec = new ExpressionContainer();
		ec.addExpression(docType, Comparison.EQ, path, "$sym", symbol);
		Map<String, String> params = new HashMap<String, String>();
		params.put(":name", "/" + prefix + ":Security/" + prefix + ":Name/text()");
		params.put(":price", "/" + prefix + ":Security/" + prefix + ":Price/" + prefix + ":PriceToday/" + prefix + ":Open/text()");
		return dMgr.getXML(ec, "<print>The open price of the security \":name\" is :price dollars</print>", params);
	}
	
	public Collection<String> getSecurity(String symbol) {
		String prefix = mDictionary.getNamespacePrefix("http://tpox-benchmark.com/security"); 
		int docType = mDictionary.getDocumentType("/" + prefix + ":Security");
		PathBuilder path = new PathBuilder().
				addPathSegment(AxisType.CHILD, prefix, "Security").
				addPathSegment(AxisType.CHILD, prefix, "Symbol").
				addPathSegment(AxisType.CHILD, null, "text()");
		ExpressionContainer ec = new ExpressionContainer();
		ec.addExpression(docType, Comparison.EQ, path, "$sym", symbol);
		Map<String, String> params = new HashMap<String, String>();
		params.put(":sec", "/" + prefix + ":Security");
		return dMgr.getXML(ec, ":sec", params);
	}
	
	public Collection<String> getOrder(String id) {
		String prefix = mDictionary.getNamespacePrefix("http://www.fixprotocol.org/FIXML-4-4"); 
		int docType = mDictionary.getDocumentType("/" + prefix + ":FIXML"); // /" + prefix + ":Order");
		PathBuilder path = new PathBuilder().
				addPathSegment(AxisType.CHILD, prefix, "FIXML").
				addPathSegment(AxisType.CHILD, prefix, "Order").
				addPathSegment(AxisType.CHILD, null, "@ID");
		ExpressionContainer ec = new ExpressionContainer();
		ec.addExpression(docType, Comparison.EQ, path, "$id", id);
		Map<String, String> params = new HashMap<String, String>();
		params.put(":order", "/" + prefix + ":FIXML/" + prefix + ":Order");
		return dMgr.getXML(ec, ":order", params);
	}
	
	public Collection<String> getCustomerProfile(String id) {
		String prefix = mDictionary.getNamespacePrefix("http://tpox-benchmark.com/custacc"); 
		int docType = mDictionary.getDocumentType("/" + prefix + ":Customer");
		PathBuilder path = new PathBuilder().
				addPathSegment(AxisType.CHILD, prefix, "Customer").
				addPathSegment(AxisType.CHILD, null, "@id");
		ExpressionContainer ec = new ExpressionContainer();
		ec.addExpression(docType, Comparison.EQ, path, "$id", id);

		String template = "<Customer_Profile CUSTOMERID=\":id\">\n" +
				"\t:name" + 
				"\t:dob" + 
				"\t:gender" + 
				"\t:langs" + 
				"\t:addrs" + 
				"\t:eadrs" + 
			"</Customer_Profile>";
		
		Map<String, String> params = new HashMap<String, String>();
		params.put(":id", "/" + prefix + ":Customer/@id");
		params.put(":name", "/" + prefix + ":Customer/" + prefix + ":Name");
		params.put(":dob", "/" + prefix + ":Customer/" + prefix + ":DateOfBirth");
		params.put(":gender", "/" + prefix + ":Customer/" + prefix + ":Gender");
		params.put(":langs", "/" + prefix + ":Customer/" + prefix + ":Languages");
		params.put(":addrs", "/" + prefix + ":Customer/" + prefix + ":Addresses");
		params.put(":eadrs", "/" + prefix + ":Customer/" + prefix + ":EmailAddresses");
		return dMgr.getXML(ec, template, params);
	}
	
	public Collection<String> getCustomerAccounts(String id) {
		String prefix = mDictionary.getNamespacePrefix("http://tpox-benchmark.com/custacc"); 
		int docType = mDictionary.getDocumentType("/" + prefix + ":Customer");
		PathBuilder path = new PathBuilder().
				addPathSegment(AxisType.CHILD, prefix, "Customer").
				addPathSegment(AxisType.CHILD, null, "@id");
		ExpressionContainer ec = new ExpressionContainer();
		ec.addExpression(docType, Comparison.EQ, path, "$id", id);

		String template = "<Customer>:id\n" +
				"\t:name" + 
				"\t<Customer_Securities>\n" +
				"\t\t<Account BALANCE=\":balance\" ACCOUNT_ID=\":accId\">\n" +
				"\t\t\t<Securities>\n" +
				"\t\t\t\t:posName" +
				"\t\t\t</Securities>\n" +
				"\t\t</Account>\n" +
				"\t</Customer_Securities>\n" + 
			"</Customer_Profile>";
		
		Map<String, String> params = new HashMap<String, String>();
		params.put(":id", "/" + prefix + ":Customer/@id");
		params.put(":name", "/" + prefix + ":Customer/" + prefix + ":Name");
		params.put(":balance", "/" + prefix + ":Customer/" + prefix + ":Accounts/" + prefix + ":Account/" + prefix + ":Balance/" + prefix + ":OnlineActualBal/text()");
		params.put(":accId", "/" + prefix + ":Customer/" + prefix + ":Accounts/" + prefix + ":Account/@id");
		params.put(":posName", "/" + prefix + ":Customer/" + prefix + ":Accounts/" + prefix + ":Account/" + prefix + ":Holdings/" + prefix + ":Position/" + prefix + ":Name");
		
		return dMgr.getXML(ec, template, params);
	}
	
	public Collection<String> searchSecurity(String sector, float peMin, float peMax, float yieldMin) {

		String prefix = mDictionary.getNamespacePrefix("http://tpox-benchmark.com/security"); 
		int docType = mDictionary.getDocumentType("/" + prefix + ":Security");
		PathBuilder path = new PathBuilder().
				addPathSegment(AxisType.CHILD, prefix, "Security");
		ExpressionContainer ec = new ExpressionContainer();
		ec.addExpression(docType, Comparison.AND, path);
		ec.addExpression(docType, Comparison.AND, path);
		path.addPathSegment(AxisType.CHILD, prefix, "SecurityInformation").
				addPathSegment(AxisType.CHILD, null, "*").
				addPathSegment(AxisType.CHILD, prefix, "Sector").
				addPathSegment(AxisType.CHILD, null, "text()");
		ec.addExpression(docType, Comparison.EQ, path, "$sec", sector);
		path = new PathBuilder().
				addPathSegment(AxisType.CHILD, prefix, "Security").
				addPathSegment(AxisType.CHILD, prefix, "PE");
		ec.addExpression(docType, Comparison.AND, path);
		path.addPathSegment(AxisType.CHILD, null, "text()");
		ec.addExpression(docType, Comparison.GE, path, "$peMin", peMin);
		ec.addExpression(docType, Comparison.LT, path, "$peMax", peMax);
		path = new PathBuilder().
				addPathSegment(AxisType.CHILD, prefix, "Security").
				addPathSegment(AxisType.CHILD, prefix, "Yield").
				addPathSegment(AxisType.CHILD, null, "text()");
		ec.addExpression(docType, Comparison.GT, path, "$yMin", yieldMin);

        String template = "<Security>\n" +
				"\t:symbol" + 
				"\t:name" + 
				"\t:type" +  
        		//{$sec/SecurityInformation//Sector}
        		//regex = "^/" + prefix + ":Security/" + prefix + ":SecurityInformation/.*/" + prefix + ":Sector$";
        		//Collection<String> sPath = mDictionary.getPathFromRegex(docType, regex);
        		//int idx = 0;
        		//for (String path : sPath) {
        		//	params.put(":sector" + idx, path);
        		//	template += "\t:sector" + idx;
        		//	idx++;
        		//}
				"\t:sector" +  
        		"\t:pe" +
        		"\t:yield" +
		"</Security>";

		Map<String, String> params = new HashMap<String, String>();
		params.put(":symbol", "/" + prefix + ":Security/" + prefix + ":Symbol");
		params.put(":name", "/" + prefix + ":Security/" + prefix + ":Name");
		params.put(":type", "/" + prefix + ":Security/" + prefix + ":SecurityType");
		params.put(":sector", "/" + prefix + ":Security/" + prefix + ":SecurityInformation//" + prefix + ":Sector");
		params.put(":pe", "/" + prefix + ":Security/" + prefix + ":PE");
   		params.put(":yield", "/" + prefix + ":Security/" + prefix + ":Yield");

		return dMgr.getXML(ec, template, params);
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
	
	@Test
	public void getPriceTest() throws IOException {
		storeSecurityTest();

		Collection<String> sec = getPrice("VFINX");
		Assert.assertNotNull(sec);
		Assert.assertTrue(sec.size() == 1);

		sec = getPrice("IBM");
		Assert.assertNotNull(sec);
		Assert.assertTrue(sec.size() == 1);

		sec = getPrice("PTTAX");
		Assert.assertNotNull(sec);
		Assert.assertTrue(sec.size() == 1);
	}

	@Test
	public void getSecurityTest() throws IOException {
		storeSecurityTest();

		Collection<String> sec = getSecurity("VFINX");
		Assert.assertNotNull(sec);
		Assert.assertTrue(sec.size() == 1);

		sec = getSecurity("IBM");
		Assert.assertNotNull(sec);
		Assert.assertTrue(sec.size() == 1);

		sec = getSecurity("PTTAX");
		Assert.assertNotNull(sec);
		Assert.assertTrue(sec.size() == 1);
	}

	@Test
	public void searchSecurityTest() throws IOException {
		storeSecurityTest();

		Collection<String> sec = searchSecurity("Technology", 25, 28, 0);
		Assert.assertNotNull(sec);
		Assert.assertTrue(sec.size() == 1);

		sec = searchSecurity("Technology", 25, 28, 1);
		Assert.assertNotNull(sec);
		Assert.assertTrue(sec.size() == 0);

		sec = searchSecurity("Technology", 28, 29, 0);
		Assert.assertNotNull(sec);
		Assert.assertTrue(sec.size() == 0);
	}

	@Test
	public void getOrderTest() throws IOException {
		storeOrderTest();
		Collection<String> sec = getOrder("103404");
		Assert.assertNotNull(sec);
		Assert.assertTrue(sec.size() == 1);
		sec = getOrder("103935");
		Assert.assertNotNull(sec);
		Assert.assertTrue(sec.size() == 1);
	}

	@Test
	public void getCustomerProfileTest() throws IOException {
		storeCustomerTest();
		Collection<String> sec = getCustomerProfile("1011");
		Assert.assertNotNull(sec);
		Assert.assertTrue(sec.size() == 1);
	}

	@Test
	public void getCustomerAccountsTest() throws IOException {
		storeCustomerTest();
		Collection<String> sec = getCustomerAccounts("1011");
		Assert.assertNotNull(sec);
		Assert.assertTrue(sec.size() == 1);
	}
	

}
