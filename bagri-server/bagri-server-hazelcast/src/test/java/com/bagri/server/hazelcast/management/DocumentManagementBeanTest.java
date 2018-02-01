package com.bagri.server.hazelcast.management;

import static com.bagri.server.hazelcast.BagriServerTestHelper.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class DocumentManagementBeanTest extends ManagementBeanTest {
	
    //private static ClassPathXmlApplicationContext servCtx;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		//System.setProperty("bdb.log.level", "trace");
        mbsc = startAdminServer();
		startCacheServer("0");
		System.out.println("----- servers started -------");
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		stopCacheServer(); //"0");
		stopAdminServer();
		//servCtx.close();
	}

	@Override
	protected ObjectName getObjectName() throws MalformedObjectNameException {
        return new ObjectName("com.bagri.db:type=Schema,name=default,kind=DocumentManagement");
	}
	
	@Override
	protected String[] getExpectedAttributes() {
		return new String[] {"Schema", "TotalCounts", "DocumentCount", "ElementCount", "SchemaSize", "Collections", "CollectionStatistics"};
	}

	protected Map<String, Object> getExpectedAttributes2() {
		Map<String, Object> map = new HashMap<>(7);
		map.put("Schema", "default");
		map.put("TotalCounts", null);
		map.put("DocumentCount", 0);
		map.put("ElementCount", 0);
		map.put("SchemaSize", 0L);
		map.put("Collections", null);
		map.put("CollectionStatistics", null);
		return map;
	}
	
	@Override
	protected String[] getExpectedOperations() {
		return new String[] {"getSchema", "clear", "getTotalCounts", "getDocumentCount", "getElementCount", "getSchemaSize", "getDocumentElements",
				"getDocumentInfo", "getDocumentLocation", "getDocumentContent", "registerDocument", "updateDocument", "removeDocument",
				"registerDocuments", "getCollections", "addCollection", "removeCollection", "enableCollection", "getCollectionStatistics",
				"resetStatistics", "addDocumentToCollection", "removeDocumentFromCollection", "getCollectionDocuments", "getDocumentUris"};
	}

	@Test
	public void testBeanAttributes() throws Exception {
        ObjectName oName = getObjectName();
        Map<String, Object> expected = getExpectedAttributes2();
        for (Map.Entry<String, Object> attr: expected.entrySet()) {
        	Object o = mbsc.getAttribute(oName, attr.getKey());
        	if (attr.getValue() != null) {
        		assertEquals(attr.getValue(), o);
        	} else {
        		System.out.println("attribute: " + attr.getKey() + " value: " + o);
        	}
        }
	}
	
}
