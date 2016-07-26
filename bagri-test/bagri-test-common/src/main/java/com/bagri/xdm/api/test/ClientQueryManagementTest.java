package com.bagri.xdm.api.test;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import com.bagri.xdm.api.ResultCursor;

public abstract class ClientQueryManagementTest extends BagriManagementTest {

	public ResultCursor getPrice(String symbol) throws Exception {
		
		String query = "declare namespace s=\"http://tpox-benchmark.com/security\";\n" +
				"declare variable $sym external;\n" +
				"for $sec in fn:collection()/s:Security\n" +
				"where $sec/s:Symbol=$sym\n" +
				"return\n" +
					"<print>The open price of the security \"{$sec/s:Name/text()}\" is {$sec/s:Price/s:PriceToday/s:Open/text()} dollars</print>";
		Map<String, Object> params = new HashMap<>();
		params.put("sym", symbol);
   		return query(query, params, null);
	}
	
	public ResultCursor getSecurity(String symbol) throws Exception {
		
		String query = "declare namespace s=\"http://tpox-benchmark.com/security\";\n" +
				"declare variable $sym external;\n" + 
				"for $sec in fn:collection()/s:Security\n" +
		  		"where $sec/s:Symbol=$sym\n" + 
				"return $sec\n";
		Map<String, Object> params = new HashMap<>();
		params.put("sym", symbol);
   		return query(query, params, null);
	}
	
	public ResultCursor getOrder(String id) throws Exception {
		
		String query = "declare namespace o=\"http://www.fixprotocol.org/FIXML-4-4\";\n" +
				"declare variable $ID external;\n" +
				"for $ord in fn:collection()/o:FIXML\n" +
				"where $ord/o:Order/@ID=$ID\n" +
				"return $ord/o:Order\n";
		Map<String, Object> params = new HashMap<>();
		params.put("ID", id);
   		return query(query, params, null);
	}
	
	public ResultCursor getCustomer(String id) throws Exception {
		
		String query = "declare namespace c=\"http://tpox-benchmark.com/custacc\";\n" +
				"declare variable $id external;\n" +
				"for $cust in fn:collection()/c:Customer\n" +
				"where $cust/@id=$id\n" +
				"return $cust\n"; 
		Map<String, Object> params = new HashMap<>();
		params.put("id", id);
   		return query(query, params, null);
	}
	
	public ResultCursor getCustomerProfile(String id) throws Exception {
		
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
		Map<String, Object> params = new HashMap<>();
		params.put("id", id);
   		return query(query, params, null);
	}
	
	public ResultCursor getCustomerAccounts(String id) throws Exception {
		
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
		Map<String, Object> params = new HashMap<>();
		params.put("id", id);
   		return query(query, params, null);
	}
	
	public ResultCursor searchSecurity(String sector, float peMin, float peMax, float yieldMin) throws Exception {

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
		Map<String, Object> params = new HashMap<>();
		params.put("sect", sector);
	    params.put("pemin", peMin);
	    params.put("pemax", peMax);
	    params.put("yield", yieldMin);
   		return query(query, params, null);
	}
	
	public ResultCursor getTodayOrderPrice(String ID) throws Exception {

		String query = "declare default element namespace \"http://www.fixprotocol.org/FIXML-4-4\";\n" +
				"declare namespace s=\"http://tpox-benchmark.com/security\";\n" +
				"declare variable $ID external;\n" +
				"for $ord in fn:collection(\"CLN_Order\")/FIXML/Order[@ID=$ID]\n" +
				"for $sec in fn:collection(\"CLN_Security\")/s:Security[s:Symbol=$ord/Instrmt/@Sym/fn:string(.)]\n" +
				"return\n" +  
				"\t<Today_Order_Price ORDER_ID=\"{$ord/@ID}\">\n" +
				"\t\t{xs:string($ord/OrdQty/@Qty*$sec/s:Price/s:PriceToday/s:Open)}\n" +
				"\t</Today_Order_Price>";
		Map<String, Object> params = new HashMap<>();
		params.put("ID", ID);
   		return query(query, params, null);
	}

	public ResultCursor getOrderCustomers(float cash, String country) throws Exception {

		String query = "declare default element namespace \"http://www.fixprotocol.org/FIXML-4-4\";\n" +
				"declare namespace c=\"http://tpox-benchmark.com/custacc\";\n" +
				"declare variable $cash external;\n" +
				"declare variable $country external;\n" +
				"for $ord in fn:collection(\"CLN_Order\")/FIXML/Order\n" +
				"for $cust in fn:collection(\"CLN_Customer\")/c:Customer[c:Accounts/c:Account/@id=$ord/@Acct/fn:string(.)]\n" +
				"where $ord/OrdQty/@Cash < $cash and $cust/c:CountryOfResidence = $country \n" + 
				"return $cust/c:ShortNames/c:ShortName";
		Map<String, Object> params = new HashMap<>();
		params.put("cash", cash);
		params.put("country", country);
   		return query(query, params, null);
	}

	public ResultCursor getCustomerByAddress(int postalCode, int orderSide, boolean isPrimary) throws Exception {

		String query = "declare default element namespace \"http://www.fixprotocol.org/FIXML-4-4\";\n" +
				"declare namespace c=\"http://tpox-benchmark.com/custacc\";\n" +
				"declare variable $pcode external;\n" +
				"declare variable $side external;\n" +
				"declare variable $kind external;\n" +
				"for $cust in fn:collection(\"CLN_Customer\")/c:Customer[c:Addresses/c:Address/c:PostalCode=$pcode]\n" +
				"for $ord in fn:collection(\"CLN_Order\")/FIXML/Order[@Acct=$cust/c:Accounts/c:Account/@id/fn:string(.) and @Side=$side]\n" +
				//(: order by $cust/c:Name/c:LastName/text() :)
				"return\n" +
				"\t<Customer>\n" +
		 		"\t\t{$cust/c:Name/c:LastName/text()} - {$cust/c:Addresses/c:Address[@primary=$kind]/c:Phones/c:Phone[@primary=$kind]}\n" +
		 		"\t</Customer>";
		Map<String, Object> params = new HashMap<>();
		params.put("pcode", postalCode);
		params.put("side", orderSide);
		params.put("kind", isPrimary ? "Yes" : "No");
   		return query(query, params, null);
	}

	
	public ResultCursor getMaxOrderPrice(int id) throws Exception {

		String query = "declare default element namespace \"http://www.fixprotocol.org/FIXML-4-4\";\n" +
				"declare namespace c=\"http://tpox-benchmark.com/custacc\";\n" +
				"declare variable $id external;\n" +
				"let $orderprice := \n" +
					"for $cust in fn:collection(\"CLN_Customer\")/c:Customer[@id=$id]\n" +
					"for $ord in fn:collection(\"CLN_Order\")/FIXML/Order[@Acct=$cust/c:Accounts/c:Account/@id/fn:string(.)]\n" +
					"return $ord/OrdQty/@Cash\n" +
				"return fn:max($orderprice)";
		Map<String, Object> params = new HashMap<>();
		params.put("id", id);
   		return query(query, params, null);
	}

	public ResultCursor getMaxOrderForIndustry(String industry, String state) throws Exception {

		String query = "declare default element namespace \"http://www.fixprotocol.org/FIXML-4-4\";\n" +
				"declare namespace s=\"http://tpox-benchmark.com/security\";\n" + 
				"declare namespace c=\"http://tpox-benchmark.com/custacc\";\n" +
				"declare variable $industry external;\n" +
				"declare variable $state external;\n" +
				"let $order := \n" +
						"for $ss in fn:collection(\"CLN_Security\")/s:Security[s:SecurityInformation/s:StockInformation/s:Industry = $industry]\n" + 
						"for $ord in fn:collection(\"CLN_Order\")/FIXML/Order[Instrmt/@Sym=$ss/s:Symbol/fn:string(.)]\n" + 
						"for $cs in fn:collection(\"CLN_Customer\")/c:Customer[c:Addresses/c:Address/c:State = $state]/c:Accounts/c:Account[@id=$ord/@Acct/fn:string(.)]\n" + 
						"return $ord/OrdQty/@Cash\n" +
				"return xs:string(fn:max($order))";
		Map<String, Object> params = new HashMap<>();
		params.put("industry", industry);
		params.put("state", state);
   		return query(query, params, null);
	}

	public ResultCursor getCustomerSecurities(int id) throws Exception {

		String query = "declare namespace s=\"http://tpox-benchmark.com/security\";\n" + 
				"declare namespace c=\"http://tpox-benchmark.com/custacc\";\n" +
				"declare variable $id external;\n" +
				"for $cust in fn:collection(\"CLN_Customer\")/c:Customer[@id=$id]\n" +
				"for $sec in fn:collection(\"CLN_Security\")/s:Security[s:Symbol=$cust/c:Accounts/c:Account/c:Holdings/c:Position/c:Symbol/fn:string(.)]\n" + 
				"return <Security>{$sec/s:Name/text()}</Security>";
		Map<String, Object> params = new HashMap<>();
		params.put("id", id);
   		return query(query, params, null);
	}
	
}
