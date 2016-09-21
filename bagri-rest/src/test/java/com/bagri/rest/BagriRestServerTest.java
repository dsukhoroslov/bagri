package com.bagri.rest;

//import com.bagri.xdm.cache.hazelcast.management.SchemaManagement;
import com.bagri.xdm.system.Schema;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Ignore;
import org.junit.Test;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Ignore
public class BagriRestServerTest extends JerseyTest {

    private RepositoryProvider repoProvider;
    //SchemaManagement schemaManagement;

    private JsonFactory jsonFactory = new JsonFactory(new ObjectMapper());

    @Override
    protected Application configure() {
        repoProvider = mock(RepositoryProvider.class);

        //when(restContext.getSchemaManagement()).thenReturn(schemaManagement);
        //when(schemaManagement.getSchemas()).thenReturn(Collections.<Schema>emptyList());
        return null; //BagriRestServer.buildConfig(restContext);
    }

    @Test
    public void test_empty_collection_json() throws Exception {
        String body = target("schema").request(MediaType.APPLICATION_JSON).get(String.class);//check status 200

        TreeNode treeNode = jsonFactory.createParser(body).readValueAsTree();

        assertThat(treeNode.get("href").toString(), is("\"/schema\""));
        assertThat(treeNode.get("items").toString(), is("[]"));
        assertThat(treeNode.get("pages").toString(), is("{\"itemsCount\":0,\"currentPage\":0,\"firstPage\":0,\"lastPage\":0}"));
    }

    @Test
    public void test_empty_collection_html() throws Exception {
        String body = target("schema").request(MediaType.TEXT_HTML).get(String.class);

        assertThat(body, is("<!DOCTYPE html>\n"
                + "<html>\n"
                + "<body>\n"
                + "<a href=\"/schema\">schema</a>\n"
                + "</body>\n"
                + "</html>"
        ));
    }
}