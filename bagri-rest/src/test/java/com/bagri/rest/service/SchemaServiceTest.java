package com.bagri.rest.service;

import com.bagri.rest.BagriRestServer;
import com.bagri.rest.RepositoryProvider;
import com.bagri.xdm.api.SchemaRepository;
import com.bagri.xdm.system.Schema;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Test;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static com.bagri.rest.service.RestService.bg_cookie;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

public class SchemaServiceTest extends JerseyTest {

	private SchemaRepository mockRepo;
	private RepositoryProvider mockPro;

    private JsonFactory jsonFactory = new JsonFactory(new ObjectMapper());

    @Override
    protected Application configure() {
        mockRepo = mock(SchemaRepository.class);
    	mockPro = mock(RepositoryProvider.class);
        when(mockPro.getRepository("client-id")).thenReturn(mockRepo);
    	when(mockRepo.getClientId()).thenReturn("client-id");
        //when(restContext.getSchemaManagement()).thenReturn(schemaManagement);
        BagriRestServer server = new BagriRestServer(mockPro, null, 3030);
        return server.buildConfig();
    }

    @Test
    public void testSchemasJson() throws Exception {
    	Collection<String> response1 = new ArrayList<>();  
        response1.add("default");
        response1.add("TPoX");
        response1.add("XMark");
		when(mockPro.getSchemaNames()).thenReturn(response1);
    	
        Response resp = target("schemas").request(MediaType.APPLICATION_JSON)
        		.cookie(bg_cookie, "client-id")
        		.get(Response.class);
        assertEquals(200, resp.getStatus());
        String body = resp.readEntity(String.class);
        TreeNode treeNode = jsonFactory.createParser(body).readValueAsTree();
        assertTrue(treeNode.isArray());
        assertEquals(3, treeNode.size());
        assertEquals("\"default\"", treeNode.get(0).toString());
        assertEquals("\"TPoX\"", treeNode.get(1).toString());
        assertEquals("\"XMark\"", treeNode.get(2).toString());

		when(mockPro.getSchema("default")).thenReturn(
				new Schema(1, new Date(), "admin", "default", "default schema for test and demo purpose", true, null));
        
        body = target("schemas").path("default")
        		.request(MediaType.APPLICATION_JSON)
        		.cookie(bg_cookie, "client-id")
        		.get(String.class);
        assertNotNull(body);
        treeNode = jsonFactory.createParser(body).readValueAsTree();
        assertEquals("\"admin\"", treeNode.get("createdBy").toString());

		when(mockPro.getSchema("TPoX")).thenReturn(
				new Schema(1, new Date(), "admin", "TPoX", "TPoX: schema for TPoX-related tests", false, null));
        
        body = target("schemas/TPoX").request(MediaType.APPLICATION_JSON)
        		.cookie(bg_cookie, "client-id")
        		.get(String.class);
        assertNotNull(body);
        treeNode = jsonFactory.createParser(body).readValueAsTree();
        assertEquals("\"admin\"", treeNode.get("createdBy").toString());

		when(mockPro.getSchema("XMark")).thenReturn(
				new Schema(1, new Date(), "admin", "XMark", "XMark benchmark schema", false, null));
        
        body = target("schemas/XMark").request(MediaType.APPLICATION_JSON)
        		.cookie(bg_cookie, "client-id")
        		.get(String.class);
        assertNotNull(body);
        treeNode = jsonFactory.createParser(body).readValueAsTree();
        assertEquals("\"admin\"", treeNode.get("createdBy").toString());
    }

}