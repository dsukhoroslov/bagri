package com.bagri.client.xqj;

import static com.bagri.common.util.FileUtils.readTextFile;

import java.io.IOException;

import javax.xml.namespace.QName;
import javax.xml.xquery.XQConnection;
import javax.xml.xquery.XQException;
import javax.xml.xquery.XQExpression;
import javax.xml.xquery.XQItemType;
import javax.xml.xquery.XQPreparedExpression;
import javax.xml.xquery.XQResultSequence;
import javax.xml.xquery.XQSequence;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class ClientApp {
	
    private static ApplicationContext context;
    private XQConnection xqc;

	public static void main(String[] args) throws XQException {
		//
		context = new ClassPathXmlApplicationContext("spring/xqj-client-context.xml");
		ClientApp client = new ClientApp(context.getBean("xqConnection", XQConnection.class));
		boolean found = false;
		try {
			//client.storeSecCommand();
			//long id = client.storeSecQuery();
			//long id = client.storeXmlDocument("axis.xml");
			//System.out.println("document stored; id: " + id);
			//found = client.runSecQuery();
			//found = client.runAxisQuery();
			found = client.searchSecQuery();
			//found = client.searchSecQueryParams();
			//client.removeSecCommand(id);
		} catch (XQException e) {
			e.printStackTrace();
		}

	    if (!found) {
	    	throw new XQException("result is empty");
	    }

		//try { 
		//	client.runPriceQuery();
		//} catch (XQException ex) {
			// expected empty result
		//	System.out.println("no document found - correct");
		//}
	}
	
	public ClientApp(XQConnection xqc) {
		this.xqc = xqc;
	}
	
	public boolean runPriceQuery() throws XQException {
		
		String query = "declare namespace s=\"http://tpox-benchmark.com/security\";\n" +
			"declare variable $sym external;\n" + 
			//"for $sec in fn:doc(\"sdoc\")/s:Security\n" +
			"for $sec in fn:collection(\"/{http://tpox-benchmark.com/security}Security\")/s:Security\n" +
	  		"where $sec/s:Symbol=$sym\n" + //'IBM'\n" +
			"return\n" +   
			"\t<print>The open price of the security \"{$sec/s:Name/text()}\" is {$sec/s:Price/s:PriceToday/s:Open/text()} dollars</print>\n";

	    XQPreparedExpression xqpe = xqc.prepareExpression(query);
	    xqpe.bindString(new QName("sym"), "IBM", null); //IBM; VFINX; PTTAX
	    XQResultSequence xqs = xqpe.executeQuery();
	    boolean found = false;
	    while (xqs.next()) {
			System.out.println(xqs.getItemAsString(null));
			found = true;
	    }
	    return found;
	}

	public boolean runSecQuery() throws XQException {
		
		String query = "declare namespace s=\"http://tpox-benchmark.com/security\";\n" +
			"declare variable $sym external;\n" + 
			//"for $sec in fn:doc(\"sdoc\")/s:Security\n" +
			"for $sec in fn:collection(\"/{http://tpox-benchmark.com/security}Security\")/s:Security\n" +
	  		"where $sec/s:Symbol=$sym\n" + //'IBM'\n" +
			"return $sec\n";

	    XQPreparedExpression xqpe = xqc.prepareExpression(query);
	    xqpe.bindString(new QName("sym"), "IBM", null);
	    XQResultSequence xqs = xqpe.executeQuery();
	    boolean found = false;
	    while (xqs.next()) {
			System.out.println(xqs.getItemAsString(null));
			found = true;
	    }
	    return found;
	}

	public boolean searchSecQuery() throws XQException {
		
		String query = "declare default element namespace \"http://tpox-benchmark.com/security\";\n" +
			"for $sec in fn:collection(\"/{http://tpox-benchmark.com/security}Security\")/Security\n" +
	  		"where $sec[SecurityInformation/*/Sector = 'Technology' and PE[. >= xs:decimal('25') and . < xs:decimal('28.0')] and Yield > xs:decimal('0')]\n" +			
			"return	<Security>\n" +	
			"\t{$sec/Symbol}\n" +
			"\t{$sec/Name}\n" +
			"\t{$sec/SecurityType}\n" +
			"\t{$sec/SecurityInformation//Sector}\n" +
			"\t{$sec/PE}\n" +
			"\t{$sec/Yield}\n" +
			"</Security>";

		XQExpression xqe = xqc.createExpression();
		XQResultSequence xqs = xqe.executeQuery(query);
		
	    boolean found = false;
	    while (xqs.next()) {
			System.out.println(xqs.getItemAsString(null));
			found = true;
	    }
	    return found;
	}

	public boolean searchSecQueryParams() throws XQException {
		
		String query = "declare default element namespace \"http://tpox-benchmark.com/security\";\n" +
			"declare variable $sect external;\n" + 
			"declare variable $pemin external;\n" +
			"declare variable $pemax external;\n" + 
			"declare variable $yield external;\n" + 
			"for $sec in fn:collection(\"/{http://tpox-benchmark.com/security}Security\")/Security\n" +
	  		"where $sec[SecurityInformation/*/Sector = $sect and PE[. >= $pemin and . < $pemax] and Yield > $yield]\n" +
			"return	<Security>\n" +	
			"\t{$sec/Symbol}\n" +
			"\t{$sec/Name}\n" +
			"\t{$sec/SecurityType}\n" +
			"\t{$sec/SecurityInformation//Sector}\n" +
			"\t{$sec/PE}\n" +
			"\t{$sec/Yield}\n" +
			"</Security>";

	    XQPreparedExpression xqpe = xqc.prepareExpression(query);
	    xqpe.bindString(new QName("sect"), "Technology", null);
	    xqpe.bindFloat(new QName("pemin"), 25,  null);
	    xqpe.bindFloat(new QName("pemax"), 28,  null);
	    xqpe.bindFloat(new QName("yield"), 0,  null);
	    XQResultSequence xqs = xqpe.executeQuery();
		
	    boolean found = false;
	    while (xqs.next()) {
			System.out.println(xqs.getItemAsString(null));
			found = true;
	    }
	    return found;
	}

	public void insertSecQuery() throws XQException {
		
		String query = "declare namespace s=\"http://tpox-benchmark.com/security\";\n" +
			"declare variable $sec external;\n" + 
			"declare variable $uri external;\n" + 
			"fn:put($sec, $uri)\n";

	    XQPreparedExpression xqpe = xqc.prepareExpression(query);
	    //xqpe.bindNode(new QName("sec"), "IBM", null);
	    xqpe.bindString(new QName("uri"), "/library/20", null);
	    
	    xqpe.executeQuery();
		//System.out.println(xqs.getAtomicValue());
	}

	public void storeSecCommand() throws XQException {

		String dName = "C:\\Work\\Bagri\\project\\trunk\\etc\\samples\\";
		String xml;
		try {
			xml = readTextFile(dName + "security5621.xml");
		} catch (IOException ex) {
			throw new XQException(ex.getMessage());
		}
		
		XQExpression xqe = xqc.createExpression();
		xqe.bindString(new QName("doc"), xml, xqc.createAtomicType(XQItemType.XQBASETYPE_STRING));
	    xqe.executeCommand("storeDocument($doc)");
	    // todo: get XDMDocument back somehow..
	    // XDMDocument doc = (XDMDocument) ...
	    xqe.close();
	}

	public long storeSecQuery() throws XQException {
		return storeXmlDocument("security5621.xml");
	}
	
	private long storeXmlDocument(String fileName) throws XQException {
		
		String dName = "C:\\Work\\Bagri\\project\\trunk\\etc\\samples\\";
		String xml;
		try {
			xml = readTextFile(dName + fileName);
		} catch (IOException ex) {
			throw new XQException(ex.getMessage());
		}

		String query = "declare namespace bgdm=\"http://bagri.com/bagri-xdm\";\n" +
				"declare variable $sec external;\n\n" + 
				//"return bgdm:store-document($sec)\n";
				"for $id in bgdm:store-document($sec)\n" +
				"return $id\n";

	    XQPreparedExpression xqpe = xqc.prepareExpression(query);
	    xqpe.bindString(new QName("sec"), xml, xqc.createAtomicType(XQItemType.XQBASETYPE_STRING));
	    XQSequence xqs = xqpe.executeQuery();
	    if (xqs.next()) {
	    	long result = xqs.getLong();
		    xqpe.close();
		    return result;
	    } else {
	    	xqpe.close();
	    	throw new XQException("no response from store-document function");
	    }
	}

	public void removeSecCommand(long docId) throws XQException {

		XQExpression xqe = xqc.createExpression();
		xqe.bindLong(new QName("docId"), docId, xqc.createAtomicType(XQItemType.XQBASETYPE_LONG));
	    xqe.executeCommand("removeDocument($docId)");
	    xqe.close();
	}
/*
	public boolean runAxisQuery() throws XQException {

		String query = //"declare namespace s=\"http://tpox-benchmark.com/security\";\n" +
				//"for $sec in fn:collection(\"/{http://tpox-benchmark.com/security}Security\")/s:Security\n" +
				"for $e in fn:collection(\"/document\")/document\n" +
		  		//"where $sec/s:Symbol=$sym\n" + //'IBM'\n" +
				"return $e//child\n";
		
		XQExpression xqe = xqc.createExpression();
		//xqe.bindString(new QName("doc"), xml, xqc.createAtomicType(XQItemType.XQBASETYPE_STRING));
	    XQResultSequence xqrs = xqe.executeQuery(query);
	    //xqe.close();

	    boolean found = false;
	    while (xqrs.next()) {
			System.out.println(xqrs.getItemAsString(null));
			found = true;
	    }
	    return found;
	}
*/
}

