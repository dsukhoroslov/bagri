package com.bagri.server.rest;

import static com.bagri.core.Constants.pn_document_data_format;
import static com.bagri.core.system.DataFormat.df_xml;
import static com.bagri.rest.RestConstants.bg_cookie;
import static org.junit.Assert.*;

import java.util.Properties;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.bagri.rest.service.DocumentBean;
import com.bagri.rest.service.DocumentParams;

public class DocumentServiceTest extends RestServiceTest {

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
    public void testDocumentCreation() throws Exception {
    	
    	Properties propsXml = new Properties();
    	propsXml.setProperty(pn_document_data_format, df_xml);
    	// create document
    	DocumentParams params = new DocumentParams("a0001.xml", "<content>initial content</content>", propsXml);
    	
        Response resp = target.path("docs").request()
        		.header("Content-Type", "application/json")
        		.cookie(bg_cookie, cuid)
        		.post(Entity.json(params));
        assertEquals(Status.CREATED.getStatusCode(), resp.getStatus());
        assertTrue(resp.getLocation().getPath().endsWith("/a0001.xml"));
        DocumentBean doc = resp.readEntity(DocumentBean.class);
        assertEquals("a0001.xml", doc.uri);
    }	
    

    @Test
    public void testDocumentSelection() throws Exception {
    	
    	Properties propsXml = new Properties();
    	propsXml.setProperty(pn_document_data_format, df_xml);

    	String query = "createdBy = admin, bytes > 3000, uri like security%, collections.contains(securities)";
        Response resp = target.path("docs").queryParam("query", query).queryParam("page", 1).queryParam("size", 50)
        		.request()
        		.header("Content-Type", "application/json")
        		.cookie(bg_cookie, cuid)
        		.get();
        // expect no results
        assertEquals(Status.NO_CONTENT.getStatusCode(), resp.getStatus());
        
    	//query = "createdBy = guest";
        //resp = target.path("docs").queryParam("query", query).queryParam("page", 1).queryParam("size", 50)
        //		.request()
        //		.header("Content-Type", "application/json")
        //		.cookie(bg_cookie, cuid)
        //		.get();
        // expect results
        //assertEquals(Status.OK.getStatusCode(), resp.getStatus());
        //DocumentBean[] docs = resp.readEntity(DocumentBean[].class);
        //assertEquals(1, docs.length);
        
        //assertTrue(resp.getLocation().getPath().endsWith("/a0001.xml"));
        //DocumentBean doc = resp.readEntity(DocumentBean.class);
        //assertEquals("a0001.xml", doc.uri);
    }	
}
