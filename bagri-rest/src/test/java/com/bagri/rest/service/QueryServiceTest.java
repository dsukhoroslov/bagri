package com.bagri.rest.service;

import static com.bagri.rest.service.RestService.bg_cookie;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;

import org.glassfish.jersey.test.JerseyTest;
import org.junit.Test;

import com.bagri.rest.BagriRestServer;
import com.bagri.rest.RepositoryProvider;
import com.bagri.xdm.api.QueryManagement;
import com.bagri.xdm.api.SchemaRepository;

public class QueryServiceTest extends JerseyTest {
	
	private QueryManagement queryMgr;
	private SchemaRepository mockRepo;
	private RepositoryProvider mockPro;
	
	private String q1 = "for $doc in fn:collection(\"CLN_Security\")\n" +
						"return $doc";

	private String q2 = "declare namespace s=\"http://tpox-benchmark.com/security\";\n" +
						"declare variable $sym external;\n" +
						"for $doc in fn:collection(\"CLN_Security\")\n" +
						"where $sec/s:Symbol=$sym\n" +
						"return $doc";
	
    @Override
    protected Application configure() {
        queryMgr = mock(QueryManagement.class);
        mockRepo = mock(SchemaRepository.class);
    	mockPro = mock(RepositoryProvider.class);
        when(mockPro.getRepository("client-id")).thenReturn(mockRepo);
        when(mockRepo.getQueryManagement()).thenReturn(queryMgr);
        BagriRestServer server = new BagriRestServer(mockPro, 3030);
        return server.buildConfig();
    }

	@Test
    @SuppressWarnings("unchecked")
    public void testQueryURIService() throws Exception {
    	//
    	Collection<String> response1 = new ArrayList<>();  
        response1.add("security1500.xml");
        response1.add("security5621.xml");
        response1.add("security9012.xml");
		when(queryMgr.getDocumentUris(q1, null, null)).thenReturn(response1);

    	QueryParams params = new QueryParams(q1, null, null);
        Collection<String> uris = target("query/uris").request()
        		.header("Content-Type", "application/json")
        		.cookie(bg_cookie, "client-id")
        		.post(Entity.json(params), Collection.class);
    	assertEquals(3, uris.size());
    	assertTrue(uris.contains("security1500.xml"));
    	assertTrue(uris.contains("security5621.xml"));
    	assertTrue(uris.contains("security9012.xml"));

    	Map<String, Object> params2 = new HashMap<>();
        params2.put("sym", "IBM");
    	Collection<String> response2 = new ArrayList<>();  
        response2.add("security5621.xml");
		when(queryMgr.getDocumentUris(q2, params2, null)).thenReturn(response2);
    	
    	params = new QueryParams(q2, params2, null);
        uris = target("query/uris").request()
        		.header("Content-Type", "application/json")
        		.cookie(bg_cookie, "client-id")
        		.post(Entity.json(params), Collection.class);
    	assertEquals(1, uris.size());
    	assertTrue(uris.contains("security5621.xml"));
    }
    
}
