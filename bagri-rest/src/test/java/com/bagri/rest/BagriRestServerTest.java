package com.bagri.rest;

import com.bagri.xdm.system.Schema;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Test;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BagriRestServerTest extends JerseyTest {

	private BagriRestServer server;
    //private RepositoryProvider repoProvider;
    //SchemaManagement schemaManagement;

    private JsonFactory jsonFactory = new JsonFactory(new ObjectMapper());

    @Override
    protected Application configure() {
        //repoProvider = mock(RepositoryProvider.class);

        //when(restContext.getSchemaManagement()).thenReturn(schemaManagement);
        //when(schemaManagement.getSchemas()).thenReturn(Collections.<Schema>emptyList());
        server = new BagriRestServer();
        //server.start();
        //this.
        return server.buildConfig();
    	
    	//return new ResourceConfig(SchemaResources.class);
    }

    @Test
    public void testSchemasJson() throws Exception {
        String body = target("schemas").request(MediaType.APPLICATION_JSON).get(String.class);//check status 200
        TreeNode treeNode = jsonFactory.createParser(body).readValueAsTree();
        assertTrue(treeNode.isArray());
        assertEquals(3, treeNode.size());
        assertEquals("\"default\"", treeNode.get(0).toString());
        assertEquals("\"TPoX\"", treeNode.get(1).toString());
        assertEquals("\"XMark\"", treeNode.get(2).toString());
        
        body = target("schemas").path("default").request(MediaType.APPLICATION_JSON).get(String.class);
        assertNotNull(body);
        treeNode = jsonFactory.createParser(body).readValueAsTree();
        assertEquals("\"admin\"", treeNode.get("createdBy").toString());

        body = target("schemas/TPoX").request(MediaType.APPLICATION_JSON).get(String.class);
        assertNotNull(body);
        treeNode = jsonFactory.createParser(body).readValueAsTree();
        assertEquals("\"admin\"", treeNode.get("createdBy").toString());

        body = target("schemas/XMark").request(MediaType.APPLICATION_JSON).get(String.class);
        assertNotNull(body);
        treeNode = jsonFactory.createParser(body).readValueAsTree();
        assertEquals("\"admin\"", treeNode.get("createdBy").toString());
    }

}