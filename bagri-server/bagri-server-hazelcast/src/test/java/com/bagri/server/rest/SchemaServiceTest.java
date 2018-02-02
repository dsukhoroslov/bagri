package com.bagri.server.rest;

import static com.bagri.rest.RestConstants.bg_cookie;
import static org.junit.Assert.*;

import java.util.Collection;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class SchemaServiceTest extends RestServiceTest {

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
	public void testSchemaOperations() throws Exception {
        Response resp = target.path("schemas").request()
        		.header("Content-Type", "application/json")
        		.cookie(bg_cookie, cuid)
        		.get();
        assertEquals(Status.OK.getStatusCode(), resp.getStatus());
        Collection<String> schemas = resp.readEntity(Collection.class);
        assertEquals(6, schemas.size());
        
        for (String schema: schemas) {
            resp = target.path("schemas").path(schema).request()
            		.header("Content-Type", "application/json")
            		.cookie(bg_cookie, cuid)
            		.get();
            assertEquals(Status.OK.getStatusCode(), resp.getStatus());
        	String json = resp.readEntity(String.class);
        	assertNotNull(json);
        	
            resp = target.path("schemas").path(schema).request()
            		.header("Content-Type", "application/xml")
            		.cookie(bg_cookie, cuid)
            		.get();
            assertEquals(Status.OK.getStatusCode(), resp.getStatus());
        	String xml = resp.readEntity(String.class);
        	assertNotNull(xml);
        	
        	//assertTrue(xml.length() > json.length());
        }
	}
	
}
