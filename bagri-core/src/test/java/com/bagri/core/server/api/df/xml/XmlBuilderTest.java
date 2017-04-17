package com.bagri.core.server.api.df.xml;

import static com.bagri.core.Constants.pn_log_level;
import static com.bagri.core.Constants.pn_schema_builder_pretty;
import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.util.List;
import java.util.Properties;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.bagri.core.model.Data;
import com.bagri.core.model.Element;
import com.bagri.core.server.api.ModelManagement;
import com.bagri.core.server.api.impl.ModelManagementImpl;

//@Ignore
public class XmlBuilderTest {

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
	        "            <comment></comment>\n" +
	        "        </phoneNumber>\n" +
	        "        <phoneNumber>\n" +
	        "            <type>fax</type>\n" +
	        "            <number>646 555-4567</number>\n" +
	        "            <comment/>\n" +
	        "        </phoneNumber>\n" +
	        "    </phoneNumbers>\n" +
	        "    <gender>\n" +
	        "        <type>male</type>\n" +
	        "    </gender>\n" +
	        "</person>\n";

	private static String auction = 
			"<?xml version=\"1.0\" standalone=\"yes\"?>\n" +
			"<site>\n" +
			"<regions>\n" +
			"<africa>\n" +
			"<item id=\"item0\">\n" +
			"<location>United States</location>\n" +
			"<quantity>1</quantity>\n" +
			"<name>duteous nine eighteen </name>\n" +
			"<payment>Creditcard</payment>\n" +
			"<description>\n" +
			"<parlist>\n" +
			"<listitem>\n" +
			"<text>\n" +
			"page rous lady idle authority capt professes stabs monster petition heave humbly removes rescue runs shady peace most piteous worser oak assembly holes patience but malice whoreson mirrors master tenants smocks yielded <keyword> officer embrace such fears distinction attires </keyword>\n" + 
			"</text>\n" +
			"</listitem>\n" +
			"<listitem>\n" +
			"<text>\n" +
			"shepherd noble supposed dotage humble servilius bitch theirs venus dismal wounds gum merely raise red breaks earth god folds closet captain dying reek\n" + 
			"</text>\n" +
			"</listitem>\n" +
			"</parlist>\n" +
			"</description>\n" +
			"<shipping>Will ship internationally, See description for charges</shipping>\n" +
			"<incategory category=\"category0\"/>\n" +
			"<incategory category=\"category0\"/>\n" +
			"<incategory category=\"category0\"/>\n" +
			"<incategory category=\"category0\"/>\n" +
			"<incategory category=\"category0\"/>\n" +
			"<mailbox>\n" +
			"<mail>\n" +
			"<from>Dominic Takano mailto:Takano@yahoo.com</from>\n" +
			"<to>Mechthild Renear mailto:Renear@acm.org</to>\n" +
			"<date>10/12/1999</date>\n" +
			"<text>\n" +
			"asses scruple learned crowns preventions half whisper logotype weapons doors factious already pestilent sacks dram atwain girdles deserts flood park lest graves discomfort sinful conceiv therewithal motion stained preventions greatly suit observe sinews enforcement <emph> armed </emph> gold gazing set almost catesby turned servilius cook doublet preventions shrunk\n" + 
			"</text>\n" +
			"</mail>\n" +
			"</mailbox>\n" +
			"</item>\n" +
			"</africa>\n" +
			"</regions>\n" +
			"</site>";

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		System.setProperty("logback.configurationFile", "test_logging.xml");
		//System.setProperty(pn_log_level, "debug");
	}

	@Test
	public void testBuildManual() throws Exception {
		ModelManagement model = new ModelManagementImpl();
		// to prepare model:
		XmlStaxParser parser = new XmlStaxParser(model);
		List<Data> data = parser.parse(xml);
		String root = data.get(0).getDataPath().getRoot();
		data.clear();
		data.add(new Data(model.getPath(root, "/person"), new Element(new int[] {0}, null)));
		data.add(new Data(model.getPath(root, "/person/firstName"), new Element(new int[] {0, 1}, null)));
		data.add(new Data(model.getPath(root, "/person/firstName/text()"), new Element(new int[] {0, 1, 1}, "John")));
		data.add(new Data(model.getPath(root, "/person/lastName"), new Element(new int[] {0, 2}, null)));
		data.add(new Data(model.getPath(root, "/person/lastName/text()"), new Element(new int[] {0, 2, 1}, "Smith")));
		data.add(new Data(model.getPath(root, "/person/age"), new Element(new int[] {0, 3}, null)));
		data.add(new Data(model.getPath(root, "/person/age/text()"), new Element(new int[] {0, 3, 1}, 25)));
		data.add(new Data(model.getPath(root, "/person/address"), new Element(new int[] {0, 4}, null)));
		data.add(new Data(model.getPath(root, "/person/address/streetAddress"), new Element(new int[] {0, 4, 1}, null)));
		data.add(new Data(model.getPath(root, "/person/address/streetAddress/text()"), new Element(new int[] {0, 4, 1, 1}, "21 2nd Street")));
		data.add(new Data(model.getPath(root, "/person/address/city"), new Element(new int[] {0, 4, 2}, null)));
		data.add(new Data(model.getPath(root, "/person/address/city/text()"), new Element(new int[] {0, 4, 2, 1}, "New York")));
		data.add(new Data(model.getPath(root, "/person/address/state"), new Element(new int[] {0, 4, 3}, null)));
		data.add(new Data(model.getPath(root, "/person/address/state/text()"), new Element(new int[] {0, 4, 3, 1}, "NY")));
		data.add(new Data(model.getPath(root, "/person/address/postalCode"), new Element(new int[] {0, 4, 4}, null)));
		data.add(new Data(model.getPath(root, "/person/address/postalCode/text()"), new Element(new int[] {0, 4, 4, 1}, "10021")));
		data.add(new Data(model.getPath(root, "/person/phoneNumbers"), new Element(new int[] {0, 5}, null)));
		data.add(new Data(model.getPath(root, "/person/phoneNumbers/phoneNumber"), new Element(new int[] {0, 5, 1}, null)));
		data.add(new Data(model.getPath(root, "/person/phoneNumbers/phoneNumber/type"), new Element(new int[] {0, 5, 1, 1}, null)));
		data.add(new Data(model.getPath(root, "/person/phoneNumbers/phoneNumber/type/text()"), new Element(new int[] {0, 5, 1, 1, 1}, "home")));
		data.add(new Data(model.getPath(root, "/person/phoneNumbers/phoneNumber/number"), new Element(new int[] {0, 5, 1, 2}, null)));
		data.add(new Data(model.getPath(root, "/person/phoneNumbers/phoneNumber/number/text()"), new Element(new int[] {0, 5, 1, 2, 1}, "212 555-1234")));
		data.add(new Data(model.getPath(root, "/person/phoneNumbers/phoneNumber"), new Element(new int[] {0, 5, 2}, null)));
		data.add(new Data(model.getPath(root, "/person/phoneNumbers/phoneNumber/type"), new Element(new int[] {0, 5, 2, 1}, null)));
		data.add(new Data(model.getPath(root, "/person/phoneNumbers/phoneNumber/type/text()"), new Element(new int[] {0, 5, 2, 1, 1}, "fax")));
		data.add(new Data(model.getPath(root, "/person/phoneNumbers/phoneNumber/number"), new Element(new int[] {0, 5, 2, 2}, null)));
		data.add(new Data(model.getPath(root, "/person/phoneNumbers/phoneNumber/number/text()"), new Element(new int[] {0, 5, 2, 2, 1}, "646 555-4567")));
		data.add(new Data(model.getPath(root, "/person/gender"), new Element(new int[] {0, 6}, null)));
		data.add(new Data(model.getPath(root, "/person/gender/type"), new Element(new int[] {0, 6, 1}, null)));
		data.add(new Data(model.getPath(root, "/person/gender/type/text()"), new Element(new int[] {0, 6, 1, 1}, "male")));
		//System.out.println(data);
		XmlBuilder builder = new XmlBuilder(model);
		Properties props = new Properties();
		props.setProperty(pn_schema_builder_pretty, "true");
		builder.init(props);
		String content = builder.buildString(data);
		//System.out.println(content);
		assertNotNull(content);
		// now compare content vs xml..
		//assertEquals(xml, content);
	}

	@Test
	public void testBuildString() throws Exception {
		ModelManagement model = new ModelManagementImpl();
		XmlStaxParser parser = new XmlStaxParser(model);
		List<Data> data = parser.parse(xml);
		assertNotNull(data);
		assertEquals(33, data.size());
		XmlBuilder builder = new XmlBuilder(model);
		Properties props = new Properties();
		props.setProperty(pn_schema_builder_pretty, "true");
		builder.init(props);
		String content = builder.buildString(data);
		System.out.println(content);
		assertNotNull(content);
		List<Data> data2 = parser.parse(content);
		assertEquals(data.size(), data2.size());
	}
	
	@Test
	public void testBuildAuction() throws Exception {
		ModelManagement model = new ModelManagementImpl();
		XmlStaxParser parser = new XmlStaxParser(model);
		List<Data> data = parser.parse(new StringReader(auction));
		assertNotNull(data);
		XmlBuilder builder = new XmlBuilder(model);
		Properties props = new Properties();
		props.setProperty(pn_schema_builder_pretty, "true");
		builder.init(props);
		String content = builder.buildString(data);
		//System.out.println(content);
		assertNotNull(content);
		List<Data> data2 = parser.parse(content);
		assertEquals(data.size(), data2.size());
	}
	
	@Test
	public void testBuildAuctionFromFile() throws Exception {
		ModelManagement model = new ModelManagementImpl();
		XmlStaxParser parser = new XmlStaxParser(model);
		File file = new File("..\\etc\\samples\\xmark\\auction.xml");
		List<Data> data = parser.parse(file);
		assertNotNull(data);
		XmlBuilder builder = new XmlBuilder(model);
		String content = builder.buildString(data);
		assertNotNull(content);
		List<Data> data2 = parser.parse(content);
		assertEquals(data.size(), data2.size());
	}

	@Test
	public void testBuildCatalog() throws Exception {
		ModelManagement model = new ModelManagementImpl();
		XmlStaxParser parser = new XmlStaxParser(model);
		InputStream fis = new FileInputStream("..\\etc\\samples\\xdm\\catalog.xml");
		List<Data> data = parser.parse(fis);
		assertNotNull(data);
		XmlBuilder builder = new XmlBuilder(model);
		Properties props = new Properties();
		props.setProperty(pn_schema_builder_pretty, "true");
		builder.init(props);
		String content = builder.buildString(data);
		System.out.println(content);
		assertNotNull(content);
		List<Data> data2 = parser.parse(content);
		assertEquals(data.size(), data2.size());
	}

}

