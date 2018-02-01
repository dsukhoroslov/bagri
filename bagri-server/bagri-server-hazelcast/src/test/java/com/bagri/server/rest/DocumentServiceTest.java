package com.bagri.server.rest;

import static com.bagri.core.Constants.pn_document_data_format;
import static com.bagri.core.system.DataFormat.df_xml;
import static com.bagri.rest.RestConstants.bg_cookie;
import static com.bagri.server.hazelcast.BagriServerTestHelper.startAdminServer;
import static com.bagri.server.hazelcast.BagriServerTestHelper.startCacheServer;
import static com.bagri.server.hazelcast.BagriServerTestHelper.stopAdminServer;
import static com.bagri.server.hazelcast.BagriServerTestHelper.stopCacheServer;
import static org.junit.Assert.*;

import java.net.URI;
import java.util.Properties;

import javax.management.MBeanServerConnection;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.glassfish.jersey.client.ClientConfig;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.bagri.rest.service.DocumentBean;
import com.bagri.rest.service.DocumentParams;
import com.bagri.rest.service.LoginParams;

//@Ignore
public class DocumentServiceTest {

    //private static MBeanServerConnection mbsc;
    private static WebTarget target;
    private static String cuid;

	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		//System.setProperty("bdb.log.level", "trace");
        startAdminServer();
		startCacheServer("0");
		
    	//URI baseURI = new URI("https://localhost:3443");
    	URI baseURI = new URI("http://localhost:3030");
    	ClientConfig clientConfig = new ClientConfig();
    	Client client = ClientBuilder.newClient(clientConfig);
    	target = client.target(baseURI);

    	LoginParams params = new LoginParams("default", "guest", "password");
        Response response = target.path("access/login").request()
        		.header("Content-Type", "application/json")
        		.post(Entity.json(params), Response.class);
        assertEquals(Status.OK.getStatusCode(), response.getStatus());

        Cookie cc = response.getCookies().get(bg_cookie);
        assertNotNull(cc);
        assertEquals(bg_cookie, cc.getName());
        cuid = cc.getValue();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		stopCacheServer(); //"0");
		stopAdminServer();
		//servCtx.close();
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
