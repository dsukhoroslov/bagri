package com.bagri.xdm.api.test;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import javax.xml.namespace.QName;

public abstract class ClientQueryManagementTest extends XDMManagementTest {

	public Iterator<?> getPrice(String symbol) throws Exception {
		
		String query = "declare namespace s=\"http://tpox-benchmark.com/security\";\n" +
				"declare variable $sym external;\n" +
				"for $sec in fn:collection()/s:Security\n" +
				"where $sec/s:Symbol=$sym\n" +
				"return\n" +
					"<print>The open price of the security \"{$sec/s:Name/text()}\" is {$sec/s:Price/s:PriceToday/s:Open/text()} dollars</print>";
		Map<QName, Object> bindings = new HashMap<>();
		bindings.put(new QName("sym"), symbol);
   		return getQueryManagement().executeQuery(query, bindings, new Properties());
	}
	
	public Iterator<?> getSecurity(String symbol) throws Exception {
		
		String query = "declare namespace s=\"http://tpox-benchmark.com/security\";\n" +
				"declare variable $sym external;\n" + 
				"for $sec in fn:collection()/s:Security\n" +
		  		"where $sec/s:Symbol=$sym\n" + 
				"return $sec\n";
		Map<QName, Object> bindings = new HashMap<>();
		bindings.put(new QName("sym"), symbol);
   		return getQueryManagement().executeQuery(query, bindings, new Properties());
	}
	
	public Iterator<?> getOrder(String id) throws Exception {
		
		String query = "declare namespace o=\"http://www.fixprotocol.org/FIXML-4-4\";\n" +
				"declare variable $ID external;\n" +
				"for $ord in fn:collection()/o:FIXML\n" +
				"where $ord/o:Order/@ID=$ID\n" +
				"return $ord/o:Order\n";
		Map<QName, Object> bindings = new HashMap<>();
		bindings.put(new QName("ID"), id);
   		return getQueryManagement().executeQuery(query, bindings, new Properties());
	}
	
	public Iterator<?> getCustomerProfile(String id) throws Exception {
		
		String query = "declare default element namespace \"http://tpox-benchmark.com/custacc\";\n" +
				"declare variable $id external;\n" +
				"for $cust in fn:collection()/Customer\n" +
				"where $cust/@id=$id\n" +
				"return\n" + 
			    	"<Customer_Profile CUSTOMERID=\"{$cust/@id}\">\n" +
			        "\t{$cust/Name}\n" +
			        "\t{$cust/DateOfBirth}\n" +
			        "\t{$cust/Gender}\n" +
			        "\t{$cust/CountryOfResidence}\n" +
			        "\t{$cust/Languages}\n" +
			        "\t{$cust/Addresses}\n" +
			        "\t{$cust/EmailAddresses}\n" +
			        "</Customer_Profile>";
		Map<QName, Object> bindings = new HashMap<>();
		bindings.put(new QName("id"), id);
   		return getQueryManagement().executeQuery(query, bindings, new Properties());
	}
	
	public Iterator<?> getCustomerAccounts(String id) throws Exception {
		
		String query = "declare default element namespace \"http://tpox-benchmark.com/custacc\";\n" +
				"declare variable $id external;\n" +
				"for $cust in fn:collection()/Customer\n" +
				"where $cust/@id=$id\n" + 
				"return\n" +
					"<Customer>{$cust/@id}\n" +
					"\t{$cust/Name}\n" +
					"\t<Customer_Securities>\n" +
					"{\n" +
					"for $account in $cust/Accounts/Account\n" +	
					"return\n" + 
					"\t\t<Account BALANCE=\"{$account/Balance/OnlineActualBal}\" ACCOUNT_ID=\"{$account/@id}\">\n" +
					"\t\t<Securities>\n" +
					"\t\t\t{$account/Holdings/Position/Name}\n" +
					"\t\t</Securities>\n" +
					"\t\t</Account>\n" +
					"}\n" +
					"\t</Customer_Securities>\n" +
					"</Customer>";
		Map<QName, Object> bindings = new HashMap<>();
		bindings.put(new QName("id"), id);
   		return getQueryManagement().executeQuery(query, bindings, new Properties());
	}
	
	public Iterator<?> searchSecurity(String sector, float peMin, float peMax, float yieldMin) throws Exception {

		String query = "declare default element namespace \"http://tpox-benchmark.com/security\";\n" +
				"declare variable $sect external;\n" + 
				"declare variable $pemin external;\n" +
				"declare variable $pemax external;\n" + 
				"declare variable $yield external;\n" + 
				"for $sec in fn:collection()/Security\n" +
		  		"where $sec[SecurityInformation/*/Sector = $sect and PE[. >= $pemin and . < $pemax] and Yield > $yield]\n" +
				"return	<Security>\n" +	
				"\t{$sec/Symbol}\n" +
				"\t{$sec/Name}\n" +
				"\t{$sec/SecurityType}\n" +
				"\t{$sec/SecurityInformation//Sector}\n" +
				"\t{$sec/PE}\n" +
				"\t{$sec/Yield}\n" +
				"</Security>";
		Map<QName, Object> bindings = new HashMap<>();
		bindings.put(new QName("sect"), sector);
	    bindings.put(new QName("pemin"), peMin);
	    bindings.put(new QName("pemax"), peMax);
	    bindings.put(new QName("yield"), yieldMin);
   		return getQueryManagement().executeQuery(query, bindings, new Properties());
	}
	
}
