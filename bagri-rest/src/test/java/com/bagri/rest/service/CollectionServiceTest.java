package com.bagri.rest.service;

import static com.bagri.rest.RestConstants.bg_cookie;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;

import org.glassfish.jersey.test.JerseyTest;
import org.junit.Test;

import com.bagri.rest.BagriRestServer;
import com.bagri.rest.RepositoryProvider;
import com.bagri.xdm.api.DocumentManagement;
import com.bagri.xdm.api.SchemaRepository;

public class CollectionServiceTest extends JerseyTest {
	
	private DocumentManagement docMgr;
	private SchemaRepository mockRepo;
	private RepositoryProvider mockPro;

    @Override
    protected Application configure() {
        docMgr = mock(DocumentManagement.class);
        mockRepo = mock(SchemaRepository.class);
    	mockPro = mock(RepositoryProvider.class);
        when(mockPro.getRepository("client-id")).thenReturn(mockRepo);
        when(mockRepo.getDocumentManagement()).thenReturn(docMgr);
        BagriRestServer server = new BagriRestServer(mockPro, null, new Properties());
        return server.buildConfig();
    }

	@Test
    @SuppressWarnings("unchecked")
    public void testCollectionService() throws Exception {

    	Collection<String> expected = new ArrayList<>();  
    	expected.add("customers");
    	expected.add("securities");
    	expected.add("orders");
		when(docMgr.getCollections()).thenReturn(expected);

        Collection<String> response = target("clns").request(MediaType.APPLICATION_JSON)
        		.cookie(bg_cookie, "client-id").get(Collection.class);
    	assertEquals(expected.size(), response.size());
    	for (String name: expected) {
        	assertTrue(response.contains(name));
    	}
		
    	expected = new ArrayList<>();  
    	expected.add("security1500.xml");
    	expected.add("security5621.xml");
    	expected.add("security9012.xml");
		when(docMgr.getCollectionDocumentUris("securities")).thenReturn(expected);

        response = target("clns").path("securities").request(MediaType.APPLICATION_JSON)
        		.cookie(bg_cookie, "client-id").get(Collection.class);
    	assertEquals(expected.size(), response.size());
    	for (String name: expected) {
        	assertTrue(response.contains(name));
    	}

		when(docMgr.removeCollectionDocuments("securities")).thenReturn(3);
    	int deleted = target("clns").path("securities").request()
        		.cookie(bg_cookie, "client-id").delete(Integer.class);
    }    


}
