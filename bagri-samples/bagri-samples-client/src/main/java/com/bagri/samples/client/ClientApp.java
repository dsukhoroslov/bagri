package com.bagri.samples.client;

import static com.bagri.common.util.FileUtils.readTextFile;
import static com.bagri.xdm.common.XDMConstants.xs_ns;
import static com.bagri.xdm.common.XDMConstants.xs_prefix;
import static com.bagri.xqj.BagriXQUtils.getBaseTypeForTypeName;

import java.io.IOException;

import javax.xml.namespace.QName;
import javax.xml.xquery.XQConnection;
import javax.xml.xquery.XQException;
import javax.xml.xquery.XQExpression;
import javax.xml.xquery.XQItemType;
import javax.xml.xquery.XQPreparedExpression;
import javax.xml.xquery.XQResultSequence;
import javax.xml.xquery.XQSequence;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.bagri.xdm.domain.XDMDocument;
import com.hazelcast.core.HazelcastInstance;

public class ClientApp {
	
    private static ClassPathXmlApplicationContext context;
    private XQConnection xqc;

	public static void main(String[] args) throws XQException {
		
		//System.setProperty("xdm.schema.members", "localhost:10600");
		System.setProperty("logback.configurationFile", "hz-client-logging.xml");
		System.setProperty("xdm.log.level", "trace");
		//
		context = new ClassPathXmlApplicationContext("spring/xqj-client-context.xml");
		XQConnection xqc = context.getBean("xqConnection", XQConnection.class);
		ClientApp client = new ClientApp(xqc);
		//HazelcastInstance hz = context.getBean("hzInstance", HazelcastInstance.class);
				
		boolean found = false;
		try {
			//client.storeSecCommand();
			long id = client.storeSecQuery();
			//long id = client.storeXmlDocument("axis.xml");
			//System.out.println("document stored; id: " + id);
			//found = client.runPriceQuery("IBM"); 
			//found &= client.runPriceQuery("IBM");
			found = client.runSecQuery("IBM");
			//found &= client.runSecQuery("VFINX");
			//found &= client.runPriceQuery("PTTAX");
			//found = client.searchSecQuery();
			//found = client.searchSecQueryParams();
			//client.searchSecQueryParams();
			//found = client.runAxisQuery();
			//client.removeSecCommand(id);
		} catch (XQException e) {
			e.printStackTrace();
		}

	    if (!found) {
	    	throw new XQException("result is empty");
	    }

	    //context.close();
	}
	
	public ClientApp(XQConnection xqc) {
		this.xqc = xqc;
	}
	
	public boolean runPriceQuery(String symbol) throws XQException {
		
		String query = "declare namespace s=\"http://tpox-benchmark.com/security\";\n" +
			"declare variable $sym external;\n" + 
			"for $sec in fn:collection(\"/{http://tpox-benchmark.com/security}Security\")/s:Security\n" +
	  		"where $sec/s:Symbol=$sym\n" + 
			"return\n" +   
			"\t<print>The open price of the security \"{$sec/s:Name/text()}\" is {$sec/s:Price/s:PriceToday/s:Open/text()} dollars</print>\n";

	    XQPreparedExpression xqpe = xqc.prepareExpression(query);
	    xqpe.bindString(new QName("sym"), symbol, null); //IBM; VFINX; PTTAX
	    XQResultSequence xqs = xqpe.executeQuery();
	    boolean found = false;
	    while (xqs.next()) {
			System.out.println(xqs.getItemAsString(null));
			found = true;
	    }
	    return found;
	}

	public boolean runSecQuery(String symbol) throws XQException {
		
		String query = "declare namespace s=\"http://tpox-benchmark.com/security\";\n" +
			"declare variable $sym external;\n" + 
			"for $sec in fn:collection(\"/{http://tpox-benchmark.com/security}Security\")/s:Security\n" +
	  		"where $sec/s:Symbol=$sym\n" + 
			"return $sec\n";

	    XQPreparedExpression xqpe = xqc.prepareExpression(query);
	    xqpe.bindString(new QName("sym"), symbol, null);
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
		
	    int cnt = 0;
	    while (xqs.next()) {
	    	cnt++;
	    }
	    System.out.println("Got " + cnt + " results");
	    return cnt > 0;
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
    	QName typeName = new QName(xs_ns, "decimal", xs_prefix);
		int baseType = getBaseTypeForTypeName(typeName);
		XQItemType type = xqc.createAtomicType(baseType, typeName, null);
	    //xqpe.bindFloat(new QName("pemin"), 25.0f, null);
	    //xqpe.bindFloat(new QName("pemax"), 28.0f, null);
	    //xqpe.bindFloat(new QName("yield"), 0.1f, null);
		xqpe.bindObject(new QName("pemin"), new java.math.BigDecimal("25.0"), type);
		xqpe.bindObject(new QName("pemax"), new java.math.BigDecimal("28.0"), type);
		xqpe.bindObject(new QName("yield"), new java.math.BigDecimal("0.1"), type);
		//xqpe.b
	    
	    XQResultSequence xqs = xqpe.executeQuery();

	    int cnt = 0;
	    while (xqs.next()) {
			//System.out.println(xqs.getItemAsString(null));
	    	cnt++;
	    }
	    System.out.println("Got " + cnt + " results");
	    return cnt > 0;
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

		String dName = "..\\..\\etc\\samples\\tpox\\";
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
		
		String dName = "..\\..\\etc\\samples\\tpox\\";
		String xml;
		try {
			xml = readTextFile(dName + fileName);
		} catch (IOException ex) {
			throw new XQException(ex.getMessage());
		}

		String query = "declare namespace bgdm=\"http://bagri.com/bagri-xdm\";\n" +
				"declare variable $xml external;\n" + 
				//"declare option bgdm:document-format \"JSON\";\n\n" + 
				"let $id := bgdm:store-document($xml)\n" +
				"return $id\n";

	    XQPreparedExpression xqpe = xqc.prepareExpression(query);
	    xqpe.bindString(new QName("xml"), xml, xqc.createAtomicType(XQItemType.XQBASETYPE_STRING));
	    XQSequence xqs = xqpe.executeQuery();
	    if (xqs.next()) {
	    	long id = xqs.getLong();
		    xqpe.close();
		    xqs.close();
		    return id;
	    } else {
	    	xqpe.close();
	    	xqs.close();
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

