package com.bagri.xquery.rex;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.bagri.xquery.rex.XQuery30RExParserLL.ParseException;
import com.bagri.xquery.rex.XQuery30RExParserLL.XmlSerializer;

public class XQuery30RExParserLLTest {

	private String query = "declare default element namespace \"http://tpox-benchmark.com/security\";\n" +
			"declare variable $sect external;\n" + // $v\n" +
			"declare variable $pemin external;\n" + // $v\n" +
			"declare variable $pemax external;\n" + // $v\n" +
			"declare variable $yield external;\n" + // $v\n" +
			//"for $sec in fn:doc(\"sdoc\")/s:Security\n" +
			"for $sec in fn:collection(\"/{http://tpox-benchmark.com/security}Security\")/Security\n" +
	  		"where $sec[SecurityInformation/*/Sector=$sect and PE[. >=$pemin and . <$pemax] and Yield>$yield]\n" +
			"return	<Security>\n" +	
			"\t{$sec/Symbol}\n" +
			"\t{$sec/Name}\n" +
			"\t{$sec/SecurityType}\n" +
			"\t{$sec/SecurityInformation//Sector}\n" +
			"\t{$sec/PE}\n" +
			"\t{$sec/Yield}\n" +
			"</Security>";
	
	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testParse_XQuery() throws IOException {
        Writer w = new OutputStreamWriter(System.out, "UTF-8");
        XmlSerializer s = new XmlSerializer(w);
        XQuery30RExParserLL parser = new XQuery30RExParserLL(query, s);
        try
        {
          s.writeOutput("<?xml version=\"1.0\" encoding=\"UTF-8\"?" + ">");
          parser.parse_XQuery();
        }
        catch (ParseException pe)
        {
          throw new RuntimeException("ParseException while processing " + query + ":\n" + parser.getErrorMessage(pe));
        }
        finally
        {
          w.close();
        }
	}

}
