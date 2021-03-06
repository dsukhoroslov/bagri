package com.bagri.rest.service;

import static com.bagri.core.Constants.pn_document_headers;
import static com.bagri.rest.RestConstants.bg_cookie;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.xml.xquery.XQItemAccessor;
import javax.xml.xquery.XQItemType;

import org.glassfish.jersey.test.JerseyTest;
import org.junit.Test;

import com.bagri.client.hazelcast.impl.DocumentAccessorImpl;
import com.bagri.client.hazelcast.impl.FixedCursorImpl;
import com.bagri.core.api.DocumentAccessor;
import com.bagri.core.api.DocumentManagement;
import com.bagri.core.api.ResultCursor;
import com.bagri.core.api.SchemaRepository;
import com.bagri.core.xquery.api.XQProcessor;
import com.bagri.rest.BagriRestServer;
import com.bagri.rest.RepositoryProvider;
import com.bagri.xqj.BagriXQDataFactory;
import com.bagri.xquery.saxon.XQProcessorClient;

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

    	Collection<String> expected = Arrays.asList("customers", "securities", "orders");
		when(docMgr.getCollections()).thenReturn(expected);

        Collection<String> response = target("clns").request(MediaType.APPLICATION_JSON)
        		.cookie(bg_cookie, "client-id").get(Collection.class);
    	assertEquals(expected.size(), response.size());
    	for (String name: expected) {
        	assertTrue(response.contains(name));
    	}
		
		Properties props = new Properties();
		props.setProperty(pn_document_headers, String.valueOf(DocumentAccessor.HDR_URI));
		List<DocumentAccessorImpl> results = Arrays.asList(
    		new DocumentAccessorImpl(null, null, null, null, 0L, null, null, 0L, 0L, 0, 0, null, 0L, 0L, "security1500.xml", 0),
    		new DocumentAccessorImpl(null, null, null, null, 0L, null, null, 0L, 0L, 0, 0, null, 0L, 0L, "security5621.xml", 0),
    		new DocumentAccessorImpl(null, null, null, null, 0L, null, null, 0L, 0L, 0, 0, null, 0L, 0L, "security9012.xml", 0));
		when(docMgr.getDocuments("collections.contains(securities), txFinish = 0", props)).thenReturn(new FixedCursorImpl(results));
	
        response = target("clns").path("securities").request(MediaType.APPLICATION_JSON)
        		.cookie(bg_cookie, "client-id").get(Collection.class);
    	assertEquals(expected.size(), response.size());
    	for (DocumentAccessor da: results) {
        	assertTrue(response.contains(da.getUri()));
    	}

    	// TODO: make it work again..
		//when(docMgr.removeDocuments("collections.contains(securities)", null)).thenReturn(3);
    	//int deleted = target("clns").path("securities").request()
        //		.cookie(bg_cookie, "client-id").delete(Integer.class);
    }    


}
