package com.bagri.xdm.access.jcache.impl;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

import junit.framework.Assert;

import org.junit.Ignore;
import org.junit.Test;


//import com.bagri.xdm.access.DataManager;
import com.bagri.xdm.domain.XDMElement;
//import com.bagri.xdm.xml.XDMStaxParser;

public class JCacheDocumentManagerTest {

	@Test
	@Ignore
	public void parseDocumentTest() throws IOException, XMLStreamException {

		String fileName = "D:\\Work\\Bagri\\xdm\\catalog.xml";
		File xml = new File(fileName);
		//XDMStaxParser parser = new XDMStaxParser();
		List<XDMElement> result = null; //parser.parse(xml);
		Assert.assertNotNull(result);
		Assert.assertTrue(result.size() == 36);
		
		JCacheDocumentManager dMgr = new JCacheDocumentManager();
		dMgr.storeAll(result);
		
		Map<Long, XDMElement> map = dMgr.execQuery("");
		Assert.assertNotNull(result);
		Assert.assertTrue(map.size() == 36);
	}

	//@Test
	public void queryDocumentTest() {
		fail("Not yet implemented");
	}
}
