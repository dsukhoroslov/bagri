package com.bagri.core.server.api.df.xml;

import static com.bagri.core.Constants.pn_log_level;
import static com.bagri.core.Constants.pn_schema_builder_pretty;
import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.xml.xquery.XQItemType;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.bagri.core.DataKey;
import com.bagri.core.model.Data;
import com.bagri.core.model.Element;
import com.bagri.core.model.Elements;
import com.bagri.core.model.NodeKind;
import com.bagri.core.model.Occurrence;
import com.bagri.core.model.Path;
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
			"<asia>\n" +
			"<item id=\"item1\">\n" +
			"<location>United States</location>\n" +
			"<quantity>1</quantity>\n" +
			"<name>great </name>\n" +
			"<payment>Money order, Cash</payment>\n" +
			"<description>\n" +
			"<text>\n" +
			"print deceit arming ros apes unjustly oregon spring hamlet containing leaves italian turn <bold> spirit model favour disposition </bold> approach charg gold promotions despair flow assured terror assembly marry concluded author debase get bourn openly gonzago wisest bane continue cries\n" + 
			"</text>\n" +
			"</description>\n" +
			"<shipping>Will ship internationally</shipping>\n" +
			"<incategory category=\"category0\"/>\n" +
			"<incategory category=\"category0\"/>\n" +
			"<incategory category=\"category0\"/>\n" +
			"<incategory category=\"category0\"/>\n" +
			"<incategory category=\"category0\"/>\n" +
			"<incategory category=\"category0\"/>\n" +
			"<mailbox>\n" +
			"<mail>\n" +
			"<from>Fumitaka Cenzer mailto:Cenzer@savera.com</from>\n" +
			"<to>Lanju Takano mailto:Takano@itc.it</to>\n" +
			"<date>02/24/2000</date>\n" +
			"<text>\n" +
			"entreaty hath fowl prescience bounds roof fiend intellect boughs caught add jests feelingly doubt trojans wisdom greatness tune worship doors fields reads canst france pay progeny wisdom stir mov impious promis clothes hangman trebonius choose men fits preparation <keyword> benefit since eclipse gates </keyword>\n" + 
			"</text>\n" +
			"</mail>\n" +
			"<mail>\n" +
			"<from>Papa Godskesen mailto:Godskesen@uwindsor.ca</from>\n" +
			"<to>Ioana Blumberg mailto:Blumberg@conclusivestrategies.com</to>\n" +
			"<date>08/02/2001</date>\n" +
			"<text>\n" +
			"jealousy back greg folded gauntlets conduct hardness across sickness peter enough royal herb embrace piteous die servilius avoid <keyword> laying chance dungeons pleasant thyself fellow purse steward heaven ambassador terrible doubtfully </keyword> milk sky clouds unbraced put sacrifices seas childish longer flout heavy pitch rosalind orderly music delivery appease\n" + 
			"</text>\n" +
			"</mail>\n" +
			"</mailbox>\n" +
			"</item>\n" +
			"<item id=\"item2\">\n" +
			"<location>Denmark</location>\n" +
			"<quantity>1</quantity>\n" +
			"<name>scarce brook </name>\n" +
			"<payment></payment>\n" +
			"<description>\n" +
			"<parlist>\n" +
			"<listitem>\n" +
			"<text>\n" +
			"senses concave valiant star further instruments bankrupts countrymen horrid costard youth necessity tend curiously waken witness navy there honest interest perceive defendant chief traffic where nuptial descent travel prepare agreed voices swears remember peerless doing <keyword> preparation rejoice </keyword>\n" + 
			"</text>\n" +
			"</listitem>\n" +
			"<listitem>\n" +
			"<text>\n" +
			"swear canker barbarian parching coxcomb excess conspiring nobles sounded consider sayings fishified prime may spirit <emph> untruths misgives choughs mew here garments tenfold </emph> error discontent hung beatrice straight muse shame deep twice mann maecenas any conveyance fingers whereupon child case <keyword> season presently victory women beating </keyword> deprive almost wed dreams slew reveal\n" + 
			"</text>\n" +
			"</listitem>\n" +
			"<listitem>\n" +
			"<text>\n" +
			"spotted attend burden camillo field enlarge stead corporal ground tormenting <bold> naturally sanctuary riddle exile coming awake senseless chance famous albans </bold> service cricket limb from clouds amongst shore penker defend quantity dumb churlish uncover swung eros figur sulphur sky birth stare negligent unction shield instance ambition gate injury fort put infants find slavish hugh see afterwards slanders chides eyes minds alb loved endure combating voyage\n" + 
			"</text>\n" +
			"</listitem>\n" +
			"<listitem>\n" +
			"<parlist>\n" +
			"<listitem>\n" +
			"<text>\n" +
			"maintained peril rivall suddenly finds studies weary truth indulgence anatomy assisted imminent may excepted yonder aches regal\n" + 
			"</text>\n" +
			"</listitem>\n" +
			"<listitem>\n" +
			"<text>\n" +
			"<bold> friar prophetess </bold> spirits delays turning cassio finding unpractis steel sweets promises credulity err nym complete star greatly mope sorry experience virtues been offending bed drives faction learnt hurl eleven huge\n" + 
			"</text>\n" +
			"</listitem>\n" +
			"<listitem>\n" +
			"<text>\n" +
			"piece hours cruelly april league winged <keyword> tract element sails course placed fouler four plac joint </keyword> words blessing fortified loving forfeit doctor valiant crying wife county planet charge haughty precious alexander longboat bells lewd kingdoms knife giver frantic raz commend sit sovereignty engaged perceive its art alliance forge bestow perforce complete roof fie confident raging possible cassio teen crave park reign lords sounded our requite fourth confidence high\n" + 
			"</text>\n" +
			"</listitem>\n" +
			"</parlist>\n" +
			"</listitem>\n" +
			"<listitem>\n" +
			"<parlist>\n" +
			"<listitem>\n" +
			"<text>\n" +
			"sent fled bids oswald help answer artillery jealous hugh fingers gladly mows our craving <emph> preventions spurr edmund drunk how faction quickly bolingbroke painfully </emph> valorous line clasp cheek patchery encompassed honest after auspicious home engaged prompt mortimer bird dread jephthah prithee unfold deeds fifty goose either herald temperance coctus took sought fail each ado checking funeral thinks linger advantage bag ridiculous along accomplishment flower glittering school disguis portia beloved crown sheets garish rather forestall vaults doublet embassy ecstasy crimson rheum befall sin devout pedro little exquisite mote messenger lancaster hideous object arrows smites gently skins bora parting desdemona longing third throng character hat sov quit mounts true house field nearest lucrece tidings fought logotype eaten commanding treason censur ripe praises windsor temperate jealous made sleeve scorn throats fits uncape tended science preventions preventions high pipes reprieves <bold> sold </bold> marriage sampson safety distrust witch christianlike plague doubling visited with bleed offenders catching attendants <emph> cars livery stand </emph> denay <keyword> cimber paper admittance tread character </keyword> battlements seen dun irish throw redeem afflicts suspicion\n" + 
			"</text>\n" +
			"</listitem>\n" +
			"<listitem>\n" +
			"<text>\n" +
			"traduc barks twenty secure pursuit believing necessities longs mental lack further observancy uncleanly understanding vault athens lucius sleeps nor safety evidence repay whensoever senses proudest restraint love mouths slaves water athenian willingly hot grieves delphos pavilion sword indeed lepidus taking disguised proffer salt before educational streets things osw rey stern lap studies finger doomsday pots bounty famous manhood observe hopes unless languish <keyword> transformed nourish breeds north </keyword>\n" + 
			"</text>\n" +
			"</listitem>\n" +
			"</parlist>\n" +
			"</listitem>\n" +
			"</parlist>\n" +
			"</description>\n" +
			"<shipping>Will ship internationally</shipping>\n" +
			"<incategory category=\"category0\"/>\n" +
			"<incategory category=\"category0\"/>\n" +
			"<mailbox>\n" +
			"<mail>\n" +
			"<from>Aspi L'Ecuyer mailto:L'Ecuyer@intersys.com</from>\n" +
			"<to>Lesley Jeris mailto:Jeris@zambeel.com</to>\n" +
			"<date>10/09/1998</date>\n" +
			"<text>\n" +
			"necessities chains rosencrantz house heed course lawn diest unvirtuous supposed sees chough swor numbers game roman soundest wrestler sky lodovico beast shivers desolate norfolk forgot paulina wars george while beggar sheath thursday capable presently his protector father orchard enemies believe drains tokens prison charge cloud stab york mild scene true devotion confidence hundred those guiltless pricks sort himself mutiny officers directive wholesome edge acts dion ride draw brings custom chapless beside sex dowry casca goods priam blasphemy prick octavia brain curer thinkest idiot inward missing conspiracy tents scab inundation caesar officer dramatis\n" + 
			"</text>\n" +
			"</mail>\n" +
			"</mailbox>\n" +
			"</item>\n" +
			"</asia>\n" + 
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
		content = content.trim();
		// now compare content vs xml..
		//assertTrue(content.startsWith("<?xml "));
		assertTrue(content.startsWith("<person>"));
		assertTrue(content.endsWith("</person>"));
	}

	@Test
	public void testPersonManual() throws Exception {
		ModelManagement model = new ModelManagementImpl();
		// to prepare model:
		XmlStaxParser parser = new XmlStaxParser(model);
		List<Data> data = parser.parse(xml);
		String root = data.get(0).getDataPath().getRoot();
		data.clear();
		data.add(new Data(new Path("/", "/person", NodeKind.document, 1, 0, 25, XQItemType.XQBASETYPE_ANYTYPE, Occurrence.onlyOne), new Element(new int[] {}, null)));
		data.add(new Data(new Path("/person", "/person", NodeKind.element, 2, 1, 0, XQItemType.XQBASETYPE_ANYTYPE, Occurrence.onlyOne), new Element(new int[] {0}, null)));
		data.add(new Data(new Path("/person/firstName", "/person", NodeKind.element, 3, 2, 4, XQItemType.XQBASETYPE_ANYTYPE, Occurrence.onlyOne), new Element(new int[] {0, 1}, null)));
		data.add(new Data(new Path("/person/firstName/text()", "/person", NodeKind.text, 4, 3, 4, XQItemType.XQBASETYPE_STRING, Occurrence.onlyOne), new Element(new int[] {0, 1, 1}, "John")));
		data.add(new Data(new Path("/person/lastName", "/person", NodeKind.element, 5, 2, 6, XQItemType.XQBASETYPE_ANYTYPE, Occurrence.onlyOne), new Element(new int[] {0, 2}, null)));
		data.add(new Data(new Path("/person/lastName/text()", "/person", NodeKind.text, 6, 5, 6, XQItemType.XQBASETYPE_STRING, Occurrence.onlyOne), new Element(new int[] {0, 2, 1}, "Smith")));
		data.add(new Data(new Path("/person/age", "/person", NodeKind.element, 7, 2, 8, XQItemType.XQBASETYPE_ANYTYPE, Occurrence.onlyOne), new Element(new int[] {0, 3}, null)));
		data.add(new Data(new Path("/person/age/text()", "/person", NodeKind.text, 8, 7, 8, XQItemType.XQBASETYPE_LONG, Occurrence.onlyOne), new Element(new int[] {0, 3, 1}, 25)));
		data.add(new Data(new Path("/person/address", "/person", NodeKind.element, 9, 2, 17, XQItemType.XQBASETYPE_ANYTYPE, Occurrence.onlyOne), new Element(new int[] {0, 4}, null)));
		data.add(new Data(new Path("/person/address/streetAddress", "/person", NodeKind.element, 10, 9, 11, XQItemType.XQBASETYPE_ANYTYPE, Occurrence.onlyOne), new Element(new int[] {0, 4, 1}, null)));
		data.add(new Data(new Path("/person/address/streetAddress/text()", "/person", NodeKind.text, 11, 10, 11, XQItemType.XQBASETYPE_STRING, Occurrence.onlyOne), new Element(new int[] {0, 4, 1, 1}, "21 2nd Street")));
		data.add(new Data(new Path("/person/address/city", "/person", NodeKind.element, 12, 9, 13, XQItemType.XQBASETYPE_ANYTYPE, Occurrence.onlyOne), new Element(new int[] {0, 4, 2}, null)));
		data.add(new Data(new Path("/person/address/city/text()", "/person", NodeKind.text, 13, 12, 13, XQItemType.XQBASETYPE_STRING, Occurrence.onlyOne), new Element(new int[] {0, 4, 2, 1}, "New York")));
		data.add(new Data(new Path("/person/address/state", "/person", NodeKind.element, 14, 9, 15, XQItemType.XQBASETYPE_ANYTYPE, Occurrence.onlyOne), new Element(new int[] {0, 4, 3}, null)));
		data.add(new Data(new Path("/person/address/state/text()", "/person", NodeKind.text, 15, 14, 15, XQItemType.XQBASETYPE_STRING, Occurrence.onlyOne), new Element(new int[] {0, 4, 3, 1}, "NY")));
		data.add(new Data(new Path("/person/address/postalCode", "/person", NodeKind.element, 16, 9, 17, XQItemType.XQBASETYPE_ANYTYPE, Occurrence.onlyOne), new Element(new int[] {0, 4, 4}, null)));
		data.add(new Data(new Path("/person/address/postalCode/text()", "/person", NodeKind.text, 17, 16, 17, XQItemType.XQBASETYPE_STRING, Occurrence.onlyOne), new Element(new int[] {0, 4, 4, 1}, "10021")));
		data.add(new Data(new Path("/person/phoneNumbers", "/person", NodeKind.element, 18, 2, 23, XQItemType.XQBASETYPE_ANYTYPE, Occurrence.onlyOne), new Element(new int[] {0, 5}, null)));
		data.add(new Data(new Path("/person/phoneNumbers/phoneNumber", "/person", NodeKind.element, 19, 18, 23, XQItemType.XQBASETYPE_ANYTYPE, Occurrence.onlyOne), new Element(new int[] {0, 5, 1}, null)));
		data.add(new Data(new Path("/person/phoneNumbers/phoneNumber/type", "/person", NodeKind.element, 20, 19, 21, XQItemType.XQBASETYPE_ANYTYPE, Occurrence.onlyOne), new Element(new int[] {0, 5, 1, 1}, null)));
		data.add(new Data(new Path("/person/phoneNumbers/phoneNumber/type/text()", "/person", NodeKind.text, 21, 20, 21, XQItemType.XQBASETYPE_STRING, Occurrence.onlyOne), new Element(new int[] {0, 5, 1, 1, 1}, "home")));
		data.add(new Data(new Path("/person/phoneNumbers/phoneNumber/number", "/person", NodeKind.element, 22, 19, 23, XQItemType.XQBASETYPE_ANYTYPE, Occurrence.onlyOne), new Element(new int[] {0, 5, 1, 2}, null)));
		data.add(new Data(new Path("/person/phoneNumbers/phoneNumber/number/text()", "/person", NodeKind.text, 23, 22, 23, XQItemType.XQBASETYPE_STRING, Occurrence.onlyOne), new Element(new int[] {0, 5, 1, 2, 1}, "212 555-1234")));
		data.add(new Data(new Path("/person/phoneNumbers/phoneNumber", "/person", NodeKind.element, 19, 18, 23, XQItemType.XQBASETYPE_ANYTYPE, Occurrence.onlyOne), new Element(new int[] {0, 5, 2}, null)));
		data.add(new Data(new Path("/person/phoneNumbers/phoneNumber/type", "/person", NodeKind.element, 20, 19, 21, XQItemType.XQBASETYPE_ANYTYPE, Occurrence.onlyOne), new Element(new int[] {0, 5, 2, 1}, null)));
		data.add(new Data(new Path("/person/phoneNumbers/phoneNumber/type/text()", "/person", NodeKind.text, 21, 20, 21, XQItemType.XQBASETYPE_STRING, Occurrence.onlyOne), new Element(new int[] {0, 5, 2, 1, 1}, "fax")));
		data.add(new Data(new Path("/person/phoneNumbers/phoneNumber/number", "/person", NodeKind.element, 22, 19, 23, XQItemType.XQBASETYPE_ANYTYPE, Occurrence.onlyOne), new Element(new int[] {0, 5, 2, 2}, null)));
		data.add(new Data(new Path("/person/phoneNumbers/phoneNumber/number/text()", "/person", NodeKind.text, 23, 22, 23, XQItemType.XQBASETYPE_STRING, Occurrence.onlyOne), new Element(new int[] {0, 5, 2, 2, 1}, "646 555-4567")));
		//data.add(new Data(new Path("/phoneNumber/comment", "/", NodeKind.attribute, 14, 11, 14, XQItemType.XQBASETYPE_STRING, Occurrence.onlyOne), new Element(new int[] {5, 2, 3}, Data._null)));
		data.add(new Data(new Path("/person/gender", "/person", NodeKind.element, 24, 2, 26, XQItemType.XQBASETYPE_ANYTYPE, Occurrence.onlyOne), new Element(new int[] {0, 6}, null)));
		data.add(new Data(new Path("/person/gender/type", "/person", NodeKind.element, 25, 24, 26, XQItemType.XQBASETYPE_ANYTYPE, Occurrence.onlyOne), new Element(new int[] {0, 6, 1}, null)));
		data.add(new Data(new Path("/person/gender/type/text()", "/person", NodeKind.text, 26, 25, 26, XQItemType.XQBASETYPE_STRING, Occurrence.onlyOne), new Element(new int[] {0, 6, 1, 1}, "male")));
		//System.out.println(data);
		XmlBuilder builder = new XmlBuilder(model);
		Properties props = new Properties();
		props.setProperty(pn_schema_builder_pretty, "true");
		builder.init(props);
		String content = builder.buildString(data);
		//System.out.println(content);
		assertNotNull(content);
		content = content.trim();
		System.out.println(content);
		// now compare content vs xml..
		assertTrue(content.startsWith("<?xml "));
		//assertTrue(content.startsWith("<person>"));
		assertTrue(content.endsWith("</person>"));
	}

	@Test
	public void testBuildString() throws Exception {
		ModelManagement model = new ModelManagementImpl();
		XmlStaxParser parser = new XmlStaxParser(model);
		List<Data> data = parser.parse(xml);
		assertNotNull(data);
		assertEquals(35, data.size());
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
		System.out.println(content);
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

	@Test
	public void testBuildManualSparse() throws Exception {
		ModelManagement model = new ModelManagementImpl();
		// to prepare model:
		XmlStaxParser parser = new XmlStaxParser(model);
		List<Data> data = parser.parse(xml);
		String root = data.get(0).getDataPath().getRoot();
		data.clear();
		data.add(new Data(model.getPath(root, "/person/firstName/text()"), new Element(new int[] {0, 1, 1}, "John")));
		data.add(new Data(model.getPath(root, "/person/lastName/text()"), new Element(new int[] {0, 2, 1}, "Smith")));
		data.add(new Data(model.getPath(root, "/person/age/text()"), new Element(new int[] {0, 3, 1}, 25)));
		data.add(new Data(model.getPath(root, "/person/address/streetAddress/text()"), new Element(new int[] {0, 4, 1, 1}, "21 2nd Street")));
		data.add(new Data(model.getPath(root, "/person/address/city/text()"), new Element(new int[] {0, 4, 2, 1}, "New York")));
		data.add(new Data(model.getPath(root, "/person/address/state/text()"), new Element(new int[] {0, 4, 3, 1}, "NY")));
		data.add(new Data(model.getPath(root, "/person/address/postalCode/text()"), new Element(new int[] {0, 4, 4, 1}, "10021")));
		data.add(new Data(model.getPath(root, "/person/phoneNumbers/phoneNumber/type/text()"), new Element(new int[] {0, 5, 1, 1, 1}, "home")));
		data.add(new Data(model.getPath(root, "/person/phoneNumbers/phoneNumber/number/text()"), new Element(new int[] {0, 5, 1, 2, 1}, "212 555-1234")));
		data.add(new Data(model.getPath(root, "/person/phoneNumbers/phoneNumber/type/text()"), new Element(new int[] {0, 5, 2, 1, 1}, "fax")));
		data.add(new Data(model.getPath(root, "/person/phoneNumbers/phoneNumber/number/text()"), new Element(new int[] {0, 5, 2, 2, 1}, "646 555-4567")));
		data.add(new Data(model.getPath(root, "/person/gender/type/text()"), new Element(new int[] {0, 6, 1, 1}, "male")));
		//System.out.println(data);
		XmlBuilder builder = new XmlBuilder(model);
		Properties props = new Properties();
		props.setProperty(pn_schema_builder_pretty, "true");
		builder.init(props);
		Map<DataKey, Elements> dataMap = XmlBuilder.dataToElements(data);
		String content = builder.buildString(dataMap);
		//System.out.println(content);
		assertNotNull(content);
		content = content.trim();
		assertTrue(content.startsWith("<?xml "));
		//assertTrue(content.startsWith("<person>"));
		assertTrue(content.endsWith("</person>"));
	}
	
	@Test
	public void testBuildAuctionSparse() throws Exception {
		ModelManagement model = new ModelManagementImpl();
		XmlStaxParser parser = new XmlStaxParser(model);
		List<Data> data = parser.parse(new StringReader(auction));
		assertNotNull(data);
		XmlBuilder builder = new XmlBuilder(model);
		Properties props = new Properties();
		props.setProperty(pn_schema_builder_pretty, "true");
		builder.init(props);
		Map<DataKey, Elements> dataMap = XmlBuilder.dataToElements(data);
		String content = builder.buildString(dataMap);
		assertNotNull(content);
		//System.out.println(content);
		List<Data> data2 = parser.parse(content);
		assertEquals(data.size(), data2.size());
	}

	@Test
	public void testBuildAuctionFromFileSparse() throws Exception {
		ModelManagement model = new ModelManagementImpl();
		XmlStaxParser parser = new XmlStaxParser(model);
		File file = new File("..\\etc\\samples\\xmark\\auction.xml");
		List<Data> data = parser.parse(file);
		assertNotNull(data);
		XmlBuilder builder = new XmlBuilder(model);
		Properties props = new Properties();
		props.setProperty(pn_schema_builder_pretty, "true");
		builder.init(props);
		Map<DataKey, Elements> dataMap = XmlBuilder.dataToElements(data);
		String content = builder.buildString(dataMap);
		assertNotNull(content);
		//System.out.println(content);
		List<Data> data2 = parser.parse(content);
		assertEquals(data.size(), data2.size());
	}
}

