package com.bagri.xquery.saxon;

import static org.junit.Assert.*;

import java.io.StringReader;

import static com.bagri.xquery.saxon.SaxonUtils.objectToItem;

import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;
import javax.xml.xquery.XQException;

import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import net.sf.saxon.Configuration;
import net.sf.saxon.dom.DocumentOverNodeInfo;
import net.sf.saxon.om.DocumentInfo;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.query.DynamicQueryContext;
import net.sf.saxon.query.StaticQueryContext;
import net.sf.saxon.query.XQueryExpression;
import net.sf.saxon.trans.XPathException;

public class SaxonQueryTest {

	@Test
	public void testPersonQuery() throws XPathException, XQException {

        Configuration config = Configuration.newConfiguration();
        StaticQueryContext sqc = config.newStaticQueryContext();
   	    DynamicQueryContext dqc = new DynamicQueryContext(config);
        dqc.setApplyFunctionConversionRulesToExternalVariables(false);

		String query = "declare base-uri \"../../etc/samples/xmark/\";\n" +
				"let $auction := fn:doc(\"auction.xml\") return\n" +
				"for $b in $auction/site/people/person[@id = 'person0'] return $b/name/text()";
   	    
		//dqc.setParameter(new StructuredQName("", "", "v"), objectToItem(Boolean.TRUE, config));
   	    XQueryExpression xqExp = sqc.compileQuery(query);
        SequenceIterator itr = xqExp.iterator(dqc);
        Item item = itr.next();
   	    assertNotNull(item);
   	    String val = item.getStringValue();
		assertEquals("Huei Demke", val);
		assertNull(itr.next());
	}	
	
	@Test
	public void testMapQuery() throws XPathException {
        Configuration config = Configuration.newConfiguration();
        config.setDefaultCollection("");
        StaticQueryContext sqc = config.newStaticQueryContext();
   	    DynamicQueryContext dqc = new DynamicQueryContext(config);
        dqc.setApplyFunctionConversionRulesToExternalVariables(false);

        //String xml = "<map>\n" +
        //		"  <boolProp>false</boolProp>\n" +
        //		"  <strProp>XYZ</strProp>\n" +
        //		"  <intProp>1</intProp>\n" +
        //		"</map>";
    	//String baseURI = sqc.getBaseURI(); 
        //StringReader sr = new StringReader(xml);
        //InputSource is = new InputSource(sr);
        //is.setSystemId(baseURI);
        //Source source = new SAXSource(is);
        //source.setSystemId(baseURI);
        //config.getGlobalDocumentPool().add(config.buildDocumentTree(source), "map.xml");
        
		String query = "declare base-uri \"../../etc/samples/xdm/\";\n" +
				"declare variable $value external;\n" +
				"for $doc in fn:collection()/map\n" +
				"where $doc/intProp = $value\n" +
				//"where $doc[intProp = $value]\n" +
				"return $doc/strProp/text()";
        
   	    XQueryExpression xqExp = sqc.compileQuery(query);
   	    dqc.setParameter(new StructuredQName("", "", "value"), objectToItem(1, config));
        SequenceIterator itr = xqExp.iterator(dqc);
        Item item = itr.next();
   	    assertNotNull(item);
   	    String val = item.getStringValue();
		assertEquals("XYZ", val);
		assertNull(itr.next());
	}
}
