package com.bagri.client.tpox.ml;

import static com.bagri.common.util.FileUtils.readTextFile;

import java.io.IOException;

import javax.xml.namespace.QName;
import javax.xml.xquery.XQConnection;
import javax.xml.xquery.XQException;
import javax.xml.xquery.XQItemType;
import javax.xml.xquery.XQPreparedExpression;
import javax.xml.xquery.XQSequence;

import org.springframework.context.support.ClassPathXmlApplicationContext;

public class MarkLogicTest {
	
    private static ClassPathXmlApplicationContext context;
    private static XQConnection xqc;

	public static void main(String[] args) {
		
		System.setProperty("xdm.schema.host", "localhost");
		System.setProperty("xdm.schema.port", "8003");
		System.setProperty("xdm.schema.mode", "conformance");
		System.setProperty("xdm.schema.username", "admin");
		System.setProperty("xdm.schema.password", "admin");
		
		context = new ClassPathXmlApplicationContext("spring/tpox-ml-context.xml");
		xqc = context.getBean("xqConnection", XQConnection.class);
		MarkLogicTest test = new MarkLogicTest();
		
		try {
			test.storeXmlDocument("security1500-2.xml");
		} catch (Exception ex) {
			System.out.println(ex);
		}
		
	}	

	private void storeXmlDocument(String fileName) throws XQException {
		
		String dName = "..\\..\\etc\\samples\\tpox\\";
		String xml;
		try {
			xml = readTextFile(dName + fileName);
		} catch (IOException ex) {
			throw new XQException(ex.getMessage());
		}

		String query = //"xquery version \"1.0-ml\";\n" + 
				"declare namespace xdmp=\"http://marklogic.com/xdmp\";\n" +
				"declare variable $fn external;\n" +
				"declare variable $sec external;\n\n" +
				"xdmp:document-insert($fn, $sec)\n";
				//"for $id in xdmp:document-insert($fn, $sec)\n" +
				//"return $id\n";

	    XQPreparedExpression xqpe = xqc.prepareExpression(query);
	    xqpe.bindString(new QName("fn"), fileName, xqc.createAtomicType(XQItemType.XQBASETYPE_STRING));
	    xqpe.bindString(new QName("sec"), xml, xqc.createAtomicType(XQItemType.XQBASETYPE_STRING));
	    XQSequence xqs = xqpe.executeQuery();
	    //if (xqs.next()) {
	    //	long result = xqs.getLong();
		    xqpe.close();
		//    return result;
	    //} else {
	    //	xqpe.close();
	    //	throw new XQException("no response from store-document function");
	    //}
	}
	
}
