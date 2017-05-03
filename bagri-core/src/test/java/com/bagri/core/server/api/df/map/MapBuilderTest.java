package com.bagri.core.server.api.df.map;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.xquery.XQItemType;

import org.junit.BeforeClass;
import org.junit.Test;

import com.bagri.core.model.Data;
import com.bagri.core.model.Element;
import com.bagri.core.model.NodeKind;
import com.bagri.core.model.Occurrence;
import com.bagri.core.model.Path;
import com.bagri.core.server.api.ModelManagement;
import com.bagri.core.server.api.impl.ModelManagementImpl;

public class MapBuilderTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		System.setProperty("logback.configurationFile", "test_logging.xml");
		//System.setProperty(pn_log_level, "trace");
	}

	@Test
	public void testBuildManual() throws Exception {
		ModelManagement model = new ModelManagementImpl();
		List<Data> data = new ArrayList<>();
		//data.add(new Data(new Path("", "/map", NodeKind.document, 1, 0, 4, XQItemType.XQBASETYPE_ANYTYPE, Occurrence.onlyOne), new Element(new int[] {}, null)));
		data.add(new Data(new Path("/firstName", "/", NodeKind.attribute, 2, 1, 2, XQItemType.XQBASETYPE_STRING, Occurrence.onlyOne), new Element(new int[] {1}, "John")));
		data.add(new Data(new Path("/lastName", "/", NodeKind.attribute, 3, 1, 3, XQItemType.XQBASETYPE_STRING, Occurrence.onlyOne), new Element(new int[] {2}, "Smith")));
		data.add(new Data(new Path("/age", "/", NodeKind.attribute, 4, 1, 4, XQItemType.XQBASETYPE_LONG, Occurrence.onlyOne), new Element(new int[] {3}, new Long(25))));
		MapBuilder builder = new MapBuilder(model);
		Map<String, Object> content = builder.buildContent(data);
		System.out.println(content);
		assertNotNull(content);
		assertEquals("John", content.get("firstName"));
		assertEquals("Smith", content.get("lastName"));
		assertEquals(25L, content.get("age"));
	}
	
}
