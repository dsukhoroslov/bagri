package com.bagri.rest.service;

import static com.bagri.rest.service.RestService.bg_cookie;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.security.NoSuchAlgorithmException;

import javax.net.ssl.SSLContext;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Test;

import com.bagri.rest.BagriRestServer;
import com.bagri.rest.RepositoryProvider;
import com.bagri.xdm.api.AccessManagement;
import com.bagri.xdm.api.SchemaRepository;

public class AccessServiceTest extends JerseyTest {

	private AccessManagement accMgr;
	private SchemaRepository mockRepo;
	private RepositoryProvider mockPro;
	
    @Override
    protected Application configure() {
    	//enable(TestProperties.)
        accMgr = mock(AccessManagement.class);
        mockRepo = mock(SchemaRepository.class);
    	mockPro = mock(RepositoryProvider.class);
   		when(mockPro.connect("default", "guest", "password")).thenReturn(mockRepo);
        when(mockPro.getRepository("client-id")).thenReturn(mockRepo);
        when(mockRepo.getAccessManagement()).thenReturn(accMgr);
    	when(mockRepo.getClientId()).thenReturn("client-id");
        BagriRestServer server = new BagriRestServer(mockPro, 3030);
        ResourceConfig app = server.buildConfig();
        //server.start();
        return app;
    }

    //@Override
    //protected Client setClient(final Client client) {
        //return this.client.getAndSet(client);
    //	ClientBuilder.newBuilder().
    //}
    
    //@Override
    //protected void configureClient(final ClientConfig config) {
    	//try {
        //	config.getProperties().put(HTTPSProperties.PROPERTY_HTTPS_PROPERTIES, new HTTPSProperties());
    	//} catch (NoSuchAlgorithmException ex) {
    	//	ex.printStackTrace();
    	//}
    //}
    
    @Test
    public void testLogin() throws Exception {
    	//
    	LoginParams params = new LoginParams("default", "guest", "password");
        Response response = target("access/login").request()
        		.header("Content-Type", "application/json")
        		.post(Entity.json(params), Response.class);
        assertEquals(Status.NOT_ACCEPTABLE.getStatusCode(), response.getStatus());
        
        // now do the same via https
        //response = target("https://localhost:3443").path("access/login").request()
        //		.header("Content-Type", "application/json")
        //		.post(Entity.json(params), Response.class);
        //assertEquals(Status.OK.getStatusCode(), response.getStatus());

        //Cookie cc = response.getCookies().get(bg_cookie);
        //assertNotNull(cc);
        //assertEquals(bg_cookie, cc.getName());
        //assertEquals("client-id", cc.getValue());
    }
	
    @Test
    public void testLogout() throws Exception {
    	//
        Response response = target("access/logout").request()
        		.post(null, Response.class);
        assertEquals(401, response.getStatus());
        
        response = target("access/logout").request()
        		.cookie(bg_cookie, "client-id")
        		.post(null, Response.class);
        assertEquals(200, response.getStatus());
        //assertNull(response.getCookies().get(bg_cookie));
        Cookie cc = response.getCookies().get(bg_cookie);
        assertNotNull(cc);
        assertEquals(bg_cookie, cc.getName());
        assertTrue(cc.toString().endsWith("Max-Age=0"));
    }
    
}
