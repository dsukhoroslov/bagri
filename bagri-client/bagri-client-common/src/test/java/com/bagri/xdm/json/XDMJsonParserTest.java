package com.bagri.xdm.json;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.List;

import org.junit.Test;

import com.bagri.xdm.client.json.XDMJsonParser;
import com.bagri.xdm.domain.XDMElement;

public class XDMJsonParserTest {

	//@BeforeClass
	//public static void setUpBeforeClass() throws Exception {
	//}

	//@AfterClass
	//public static void tearDownAfterClass() throws Exception {
	//}

	//@Before
	//public void setUp() throws Exception {
	//}

	//@After
	//public void tearDown() throws Exception {
	//}


	@Test
	public void testParse() {
		XDMJsonParser parser = new XDMJsonParser();
		String json = 
		"{\n" +
		"\t\"name\":\"mkyong\",\n" +
		"\t\"age\":29,\n" +
		"\t\"messages\":[\"msg 1\",\"msg 2\",\"msg 3\"]\n" +
		"}";
		try {
			List<XDMElement> elts = parser.parse(json);
			assertNull(elts);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
