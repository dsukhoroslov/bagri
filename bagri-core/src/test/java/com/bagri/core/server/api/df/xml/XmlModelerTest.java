package com.bagri.core.server.api.df.xml;

import static com.bagri.support.util.FileUtils.readTextFile;
import static org.junit.Assert.*;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.bagri.core.model.Data;
import com.bagri.core.model.Path;
import com.bagri.core.server.api.ContentModeler;
import com.bagri.core.server.api.ModelManagement;
import com.bagri.core.server.api.impl.ModelManagementImpl;

public class XmlModelerTest {

	private static String xml = 
			"<person>\n" +
	        "    <firstName>John</firstName>\n" +
	        "    <lastName>Smith</lastName>\n" +
	        "    <age>25</age>\n" +
	        "    <address>\n" +
	        "        <streetAddress>21 2nd Street</streetAddress>\n" +
	        "        <city>New York</city>\n" +
	        "        <state>NY</state>\n" +
	        "        <postalCode>10021</postalCode>\n" +
	        "    </address>\n" +
	        "    <phoneNumbers>\n" +
	        "        <phoneNumber>\n" +
	        "            <type>home</type>\n" +
	        "            <number>212 555-1234</number>\n" +
	        "            <comment/>\n" +
	        "        </phoneNumber>\n" +
	        "    </phoneNumbers>\n" +
	        "    <gender>\n" +
	        "        <type>male</type>\n" +
	        "    </gender>\n" +
	        "</person>\n";
	
	private static String schemaRussianDoll = 
			"<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" elementFormDefault=\"qualified\" attributeFormDefault=\"unqualified\">\n" +
			"  <xs:element name=\"person\">\n" +
			"    <xs:complexType>\n" +
            "      <xs:sequence>\n" +
            "        <xs:element name=\"firstName\" type=\"xs:string\"/>\n" +
            "        <xs:element name=\"lastName\" type=\"xs:string\"/>\n" +
            "        <xs:element name=\"age\" type=\"xs:int\"/>\n" +
            "        <xs:element name=\"address\">\n" +
            "          <xs:complexType>\n" +
            "            <xs:sequence>\n" +
            "              <xs:element name=\"streetAddress\" type=\"xs:string\"/>\n" +
            "              <xs:element name=\"city\" type=\"xs:string\"/>\n" +
            "              <xs:element name=\"state\" type=\"xs:string\"/>\n" +
            "              <xs:element name=\"postalCode\" type=\"xs:int\"/>\n" +
            "            </xs:sequence>\n" +
            "          </xs:complexType>\n" +
            "        </xs:element>\n" +
            "        <xs:element name=\"phoneNumbers\">\n" +
            "          <xs:complexType>\n" +
            "            <xs:sequence>\n" +
            "              <xs:element name=\"phoneNumber\" maxOccurs=\"unbounded\">\n" +
            "                <xs:complexType>\n" +
            "                  <xs:sequence>\n" +
            "                    <xs:element name=\"type\" type=\"xs:string\"/>\n" +
            "                    <xs:element name=\"number\" type=\"xs:string\"/>\n" +
            "                    <xs:element name=\"comment\" type=\"xs:string\" minOccurs=\"0\"/>\n" +
            "                  </xs:sequence>\n" +
            "                </xs:complexType>\n" +
            "              </xs:element>\n" +
            "            </xs:sequence>\n" +
            "          </xs:complexType>\n" +
            "        </xs:element>\n" +
            "        <xs:element name=\"gender\">\n" +
            "          <xs:complexType>\n" +
            "            <xs:sequence>\n" +
            "              <xs:element name=\"type\" type=\"xs:string\"/>\n" +
            "            </xs:sequence>\n" +
            "          </xs:complexType>\n" +
            "        </xs:element>\n" +
            "      </xs:sequence>\n" +
            "    </xs:complexType>\n" +
            "  </xs:element>\n" +
            "</xs:schema>";	
	
	private static String schemaSalamiSlice = 
			"<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" elementFormDefault=\"qualified\" attributeFormDefault=\"unqualified\">\n" +
	  		"  <xs:element name=\"streetAddress\" type=\"xs:string\"/>\n" +
	  		"  <xs:element name=\"city\" type=\"xs:string\"/>\n" +
	  		"  <xs:element name=\"state\" type=\"xs:string\"/>\n" +
	  		"  <xs:element name=\"postalCode\" type=\"xs:short\"/>\n" +
	  		"  <xs:element name=\"type\" type=\"xs:string\"/>\n" +
	  		"  <xs:element name=\"number\" type=\"xs:string\"/>\n" +
	  		"  <xs:element name=\"comment\" type=\"xs:string\"/>\n" +
	  		"  <xs:element name=\"phoneNumber\">\n" +
	    	"    <xs:complexType>\n" +
	      	"      <xs:sequence>\n" +
	        "        <xs:element ref=\"type\"/>\n" +
	        "        <xs:element ref=\"number\"/>\n" +
	        "        <xs:element ref=\"comment\"/>\n" +
	        "      </xs:sequence>\n" +
	        "    </xs:complexType>\n" +
	        "  </xs:element>\n" +
	  		"  <xs:element name=\"firstName\" type=\"xs:string\"/>\n" +
	  		"  <xs:element name=\"lastName\" type=\"xs:string\"/>\n" +
	  		"  <xs:element name=\"age\" type=\"xs:byte\"/>\n" +
	  		"  <xs:element name=\"address\">\n" +
	    	"    <xs:complexType>\n" +
	      	"      <xs:sequence>\n" +
	        "        <xs:element ref=\"streetAddress\"/>\n" +
	        "        <xs:element ref=\"city\"/>\n" +
	        "        <xs:element ref=\"state\"/>\n" +
	        "        <xs:element ref=\"postalCode\"/>\n" +
	        "      </xs:sequence>\n" +
	        "    </xs:complexType>\n" +
	        "  </xs:element>\n" +
	  		"  <xs:element name=\"phoneNumbers\">\n" +
	    	"    <xs:complexType>\n" +
	      	"      <xs:sequence>\n" +
	        "        <xs:element ref=\"phoneNumber\"/>\n" +
	      	"      </xs:sequence>\n" +
	    	"    </xs:complexType>\n" +
	  		"  </xs:element>\n" +
	  		"  <xs:element name=\"gender\">\n" +
	    	"    <xs:complexType>\n" +
	      	"      <xs:sequence>\n" +
	        "        <xs:element ref=\"type\"/>\n" +
	      	"      </xs:sequence>\n" +
	    	"    </xs:complexType>\n" +
	  		"  </xs:element>\n" +
	  		"  <xs:element name=\"person\">\n" +
	    	"    <xs:complexType>\n" +
	      	"      <xs:sequence>\n" +
	        "        <xs:element ref=\"firstName\"/>\n" +
	        "        <xs:element ref=\"lastName\"/>\n" +
	        "        <xs:element ref=\"age\"/>\n" +
	        "        <xs:element ref=\"address\"/>\n" +
	        "        <xs:element ref=\"phoneNumbers\"/>\n" +
	        "        <xs:element ref=\"gender\"/>\n" +
	      	"      </xs:sequence>\n" +
	    	"    </xs:complexType>\n" +
	  		"  </xs:element>\n" +
			"</xs:schema>";
		
	private static String schemaVenetianBlind = 
			"<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" elementFormDefault=\"qualified\" attributeFormDefault=\"unqualified\">\n" +
			"  <xs:element name=\"person\" type=\"personType\"/>\n" +
			"  <xs:complexType name=\"addressType\">\n" +
			"    <xs:sequence>\n" +
	        "      <xs:element name=\"streetAddress\" type=\"xs:string\"/>\n" +
	        "      <xs:element name=\"city\" type=\"xs:string\"/>\n" +
	        "      <xs:element name=\"state\" type=\"xs:string\"/>\n" +
	        "      <xs:element name=\"postalCode\" type=\"xs:int\"/>\n" +
	        "    </xs:sequence>\n" +
	        "  </xs:complexType>\n" +
	        "  <xs:complexType name=\"phoneNumberType\">\n" +
	        "    <xs:sequence>\n" +
	        "      <xs:element name=\"type\" type=\"xs:string\"/>\n" +
	        "      <xs:element name=\"number\" type=\"xs:string\"/>\n" +
	        "      <xs:element name=\"comment\" type=\"xs:string\"/>\n" +
	        "    </xs:sequence>\n" +
	        "  </xs:complexType>\n" +
	        "  <xs:complexType name=\"phoneNumbersType\">\n" +
	        "    <xs:sequence>\n" +
	        "      <xs:element name=\"phoneNumber\" type=\"phoneNumberType\"/>\n" +
	        "    </xs:sequence>\n" +
	        "  </xs:complexType>\n" +
	        "  <xs:complexType name=\"genderType\">\n" +
	        "    <xs:sequence>\n" +
	        "      <xs:element name=\"type\" type=\"xs:string\"/>\n" +
	        "    </xs:sequence>\n" +
	        "  </xs:complexType>\n" +
	        "  <xs:complexType name=\"personType\">\n" +
	        "    <xs:sequence>\n" +
	        "      <xs:element name=\"firstName\" type=\"xs:string\"/>\n" +
	        "      <xs:element name=\"lastName\" type=\"xs:string\"/>\n" +
	        "      <xs:element name=\"age\" type=\"xs:byte\"/>\n" +
	        "      <xs:element name=\"address\" type=\"addressType\"/>\n" +
	        "      <xs:element name=\"phoneNumbers\" type=\"phoneNumbersType\"/>\n" +
	        "      <xs:element name=\"gender\" type=\"genderType\"/>\n" +
	        "    </xs:sequence>\n" +
	        "  </xs:complexType>\n" +
	        "</xs:schema>";			
	
	private ModelManagement model;
	private ContentModeler modelPro;
	
	@Before
	public void setUp() throws Exception {
		model = new ModelManagementImpl();
		modelPro = new XmlModeler(model);
	}

	//@After
	//public void tearDown() throws Exception {
	//}
	
	@Test
	public void registerRRPersonSchemaTest() throws Exception {
		XmlStaxParser parser = new XmlStaxParser(model);
		List<Data> data = parser.parse(xml);
		modelPro.registerModel(schemaRussianDoll);
		Collection<Path> paths = model.getTypePaths("/person");
		assertNotNull(paths);
		assertTrue(paths.size() > 0);
		assertEquals(data.size(), paths.size());
		compareModels(paths, data);
	}
	
	@Test
	public void registerRRSchemaPersonTest() throws Exception {
		modelPro.registerModel(schemaRussianDoll);
		Collection<Path> paths = model.getTypePaths("/person");
		//System.out.println(paths);
		assertNotNull(paths);
		assertTrue(paths.size() > 0);
		XmlStaxParser parser = new XmlStaxParser(model);
		List<Data> data = parser.parse(xml);
		assertEquals(data.size(), paths.size());
		compareModels(paths, data);
	}
	
	@Test
	public void registerSSPersonSchemaTest() throws Exception {
		XmlStaxParser parser = new XmlStaxParser(model);
		List<Data> data = parser.parse(xml);
		modelPro.registerModel(schemaSalamiSlice);
		Collection<Path> paths = model.getTypePaths("/person");
		assertNotNull(paths);
		assertTrue(paths.size() > 0);
		assertEquals(data.size(), paths.size());
		compareModels(paths, data);
	}
	
	@Test
	public void registerSSSchemaPersonTest() throws Exception {
		modelPro.registerModel(schemaSalamiSlice);
		Collection<Path> paths = model.getTypePaths("/person");
		//System.out.println(paths);
		assertNotNull(paths);
		assertTrue(paths.size() > 0);
		XmlStaxParser parser = new XmlStaxParser(model);
		List<Data> data = parser.parse(xml);
		assertEquals(data.size(), paths.size());
		compareModels(paths, data);
	}

	@Test
	public void registerVBPersonSchemaTest() throws Exception {
		XmlStaxParser parser = new XmlStaxParser(model);
		List<Data> data = parser.parse(xml);
		modelPro.registerModel(schemaVenetianBlind);
		Collection<Path> paths = model.getTypePaths("/person");
		assertNotNull(paths);
		assertTrue(paths.size() > 0);
		assertEquals(data.size(), paths.size());
		compareModels(paths, data);
	}
	
	@Test
	public void registerVBSchemaPersonTest() throws Exception {
		modelPro.registerModel(schemaVenetianBlind);
		Collection<Path> paths = model.getTypePaths("/person");
		//System.out.println(paths);
		assertNotNull(paths);
		assertTrue(paths.size() > 0);
		XmlStaxParser parser = new XmlStaxParser(model);
		List<Data> data = parser.parse(xml);
		assertEquals(data.size(), paths.size());
		compareModels(paths, data);
	}
	
	private void compareModels(Collection<Path> paths, List<Data> data) {
		for (Data dt: data) {
			Path path = findPath(paths, dt.getPathId());
			if (path == null) {
				fail("no path found for pathId " + dt.getPathId());
			}
			if (path.getParentId() != dt.getParentPathId()) {
				fail("parents are different for pathId " + dt.getPathId());
			}
			if (path.getPostId() != dt.getPostId()) {
				fail("posts are different for pathId " + dt.getPathId());
			}
		}
	}
	
	private Path findPath(Collection<Path> paths, int pathId) {
		for (Iterator<Path> itr = paths.iterator(); itr.hasNext();) {
			Path path = itr.next(); 
			if (path.getPathId() == pathId) {
				return path; 
			}
		}
		return null;
	}

	@Test
	public void registerSecurityPathTest() throws Exception {
		String schema = readTextFile("..\\etc\\samples\\tpox\\security.xsd");
		modelPro.registerModel(schema);
		Collection<Path> paths = model.getTypePaths("/{http://tpox-benchmark.com/security}Security");
		assertNotNull(paths);
		assertTrue(paths.size() > 0);
		assertEquals(165, paths.size());
	}

	@Test
	public void registerSecurityModelTest() throws Exception {
		modelPro.registerModelUri("..\\etc\\samples\\tpox\\security.xsd");
		Collection<Path> paths = model.getTypePaths("/{http://tpox-benchmark.com/security}Security");
		assertNotNull(paths);
		assertTrue(paths.size() > 0);
		assertEquals(165, paths.size());
	}

	@Test
	public void registerCustomerPathTest() throws Exception {
		String schema = readTextFile("..\\etc\\samples\\tpox\\custacc.xsd");
		modelPro.registerModel(schema);
		Collection<Path> paths = model.getTypePaths("/{http://tpox-benchmark.com/custacc}Customer");
		assertNotNull(paths);
		assertTrue(paths.size() > 0);
		assertEquals(162, paths.size());
	}

	@Test
	public void registerCustomerModelTest() throws Exception {
		modelPro.registerModelUri("..\\etc\\samples\\tpox\\custacc.xsd");
		Collection<Path> paths = model.getTypePaths("/{http://tpox-benchmark.com/custacc}Customer");
		assertNotNull(paths);
		assertTrue(paths.size() > 0);
		assertEquals(162, paths.size());
	}

	@Test
	public void registerSchemas() throws Exception {
		((XmlModeler) modelPro).registerModels("..\\etc\\samples\\tpox\\");
		Collection<Path> paths = model.getTypePaths("/{http://tpox-benchmark.com/custacc}Customer");
		assertNotNull(paths);
		assertTrue(paths.size() > 0);
		assertEquals(162, paths.size());
		paths = model.getTypePaths("/{http://tpox-benchmark.com/security}Security");
		assertNotNull(paths);
		assertTrue(paths.size() > 0);
		assertEquals(165, paths.size());
	}
}
