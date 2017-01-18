package com.bagri.xquery;

import static org.junit.Assert.*;

import org.junit.Test;

public class XQParserTest {

	@Test
	public void testMain() throws Exception {
		String xquery = 
		  		  "{for $b in document(\"http://www.bn.com\")/bib/book\n" +
		  		  "where $b/publisher = \"Addison-Wesley\" and $b/@year > 1991\n" +
		  		  "return\n" +
		  		  "  <book year=\"{ $b/@year }\">" +
		  		  "   { $b/title }" +
		  		  "  </book>}";
		XQTestParser.main(new String[] {xquery}); 
	}

}
