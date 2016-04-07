package com.bagri.xquery.saxon;

import static org.junit.Assert.*;

import javax.xml.xquery.XQException;

import org.junit.Test;

import net.sf.saxon.Configuration;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.SequenceIterator;
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

		String query = "declare base-uri \"file:/C:/Work/Bagri/git/bagri/etc/samples/xmark/\";\n" +
				"let $auction := fn:doc(\"auction.xml\") return\n" +
				"for $b in $auction/site/people/person[@id = 'person0'] return $b/name/text()";
   	    
		//dqc.setParameter(new StructuredQName("",  "", "v"), SaxonUtils.objectToItem(Boolean.TRUE, config));
   	    XQueryExpression xqExp = sqc.compileQuery(query);
        SequenceIterator itr = xqExp.iterator(dqc);
        Item item = itr.next();
   	    assertNotNull(item);
   	    String val = item.getStringValue();
		assertEquals("Huei Demke", val);
		assertNull(itr.next());
	}	
}
