package com.bagri.server.rest;

import static com.bagri.rest.RestConstants.bg_cookie;
import static com.bagri.server.hazelcast.BagriServerTestHelper.startAdminServer;
import static com.bagri.server.hazelcast.BagriServerTestHelper.startCacheServer;
import static com.bagri.server.hazelcast.BagriServerTestHelper.stopAdminServer;
import static com.bagri.server.hazelcast.BagriServerTestHelper.stopCacheServer;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.URI;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.glassfish.jersey.client.ClientConfig;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.bagri.rest.service.LoginParams;

public abstract class RestServiceTest {

	protected static ClassPathXmlApplicationContext ctx;
    protected static WebTarget target;
    protected static String cuid;
    
	//static {
	//    //for localhost testing only
	//    javax.net.ssl.HttpsURLConnection.setDefaultHostnameVerifier(
	//    new javax.net.ssl.HostnameVerifier(){
    //
	//        public boolean verify(String hostname, javax.net.ssl.SSLSession sslSession) {
	//            if (hostname.equals("localhost")) {
	//                return true;
	//            }
	//            return false;
	//        }
	//    });
	//}    

	public static void startServices() throws Exception {
		//System.setProperty("bdb.log.level", "trace");
        startAdminServer();
		ctx = startCacheServer("0");
		
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

	public static void stopServices() throws Exception {
        Response response = target.path("access/logout").request()
        		.cookie(bg_cookie, cuid)
        		.post(null, Response.class);
        assertEquals(200, response.getStatus());
        //assertNull(response.getCookies().get(bg_cookie));
        //Cookie cc = response.getCookies().get(bg_cookie);
        //assertNotNull(cc);
        //assertEquals(bg_cookie, cc.getName());
        //assertTrue(cc.toString().endsWith("Max-Age=0"));

		stopCacheServer(); //"0");
		stopAdminServer();
	}
	
}
