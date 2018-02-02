package com.bagri.server.rest;

import static com.bagri.core.Constants.*;
import static com.bagri.core.system.DataFormat.df_xml;
import static com.bagri.rest.RestConstants.bg_cookie;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.Properties;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.ApplicationContext;

import com.bagri.core.api.DocumentAccessor;
import com.bagri.core.server.api.DocumentManagement;
import com.bagri.server.hazelcast.impl.SchemaRepositoryImpl;
import com.bagri.server.hazelcast.util.SpringContextHolder;


public class CollectionServiceTest extends RestServiceTest {

	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		//System.setProperty("bdb.log.level", "trace");
		startServices();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		stopServices();
	}

	@Test
	public void testCollectionOperations() throws Exception {
        Response resp = target.path("clns").request()
        		.header("Content-Type", "application/json")
        		.cookie(bg_cookie, cuid)
        		.get();
        assertEquals(Status.OK.getStatusCode(), resp.getStatus());
        Collection<String> clns = resp.readEntity(Collection.class);
        assertEquals(3, clns.size());
        assertTrue(clns.contains("CLN_Order"));
        assertTrue(clns.contains("CLN_Customer"));
        assertTrue(clns.contains("CLN_Security"));
        
    	Properties props = new Properties();
    	props.setProperty(pn_document_data_format, df_xml);
    	props.setProperty(pn_document_collections, "CLN_Order");
    	props.setProperty(pn_document_headers, String.valueOf(DocumentAccessor.HDR_CREATED_BY | DocumentAccessor.HDR_URI));
    	// create documents
    	
    	ApplicationContext ctx = SpringContextHolder.getContext("default");
		SchemaRepositoryImpl repo = ctx.getBean(SchemaRepositoryImpl.class);
		repo.setClientId(cuid);
		DocumentManagement dm = (DocumentManagement) repo.getDocumentManagement();
        for (int i=0; i < 100; i++) {
        	String uri = "xDoc" + i + ".xml";
        	String content = "<doc id=\"" + i + "\">document content</doc>";
        	DocumentAccessor da = dm.storeDocument(uri, content, props);
        	assertEquals(uri, da.getUri());
        	assertEquals("guest", da.getCreatedBy());
        }
        
        resp = target.path("clns").path("CLN_Order").request()
        		.header("Content-Type", "application/json")
        		.cookie(bg_cookie, cuid)
        		.get();
        assertEquals(Status.OK.getStatusCode(), resp.getStatus());
        Collection<String> docs = resp.readEntity(Collection.class);
        assertEquals(100, docs.size());

        resp = target.path("clns").path("CLN_Order").request()
        		.header("Content-Type", "application/json")
        		.cookie(bg_cookie, cuid)
        		.delete();
        assertEquals(Status.OK.getStatusCode(), resp.getStatus());

        resp = target.path("clns").path("CLN_Order").request()
        		.header("Content-Type", "application/json")
        		.cookie(bg_cookie, cuid)
        		.get();
        assertEquals(Status.OK.getStatusCode(), resp.getStatus());
        docs = resp.readEntity(Collection.class);
        assertEquals(0, docs.size());
	}
	
}
