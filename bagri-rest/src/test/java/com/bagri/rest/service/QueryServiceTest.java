package com.bagri.rest.service;

import static com.bagri.rest.service.RestService.bg_cookie;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import javax.xml.xquery.XQItem;
import javax.xml.xquery.XQItemType;

import org.glassfish.jersey.client.ChunkedInput;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Ignore;
import org.junit.Test;

import com.bagri.rest.BagriRestServer;
import com.bagri.rest.RepositoryProvider;
import com.bagri.xdm.api.QueryManagement;
import com.bagri.xdm.api.SchemaRepository;
import com.bagri.xdm.client.hazelcast.impl.FixedCursorImpl;
import com.bagri.xqj.BagriXQDataFactory;
import com.bagri.xquery.api.XQProcessor;
import com.bagri.xquery.saxon.XQProcessorClient;

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
        BagriRestServer server = new BagriRestServer(mockPro, null, 3030);
        return server.buildConfig();
    }
    
	@Test
	@Ignore
    @SuppressWarnings("unchecked")
    public void testQueryService() throws Exception {
		
    	List response1 = new ArrayList<>();  
		XQProcessor proc = new XQProcessorClient();
		BagriXQDataFactory xqFactory = new BagriXQDataFactory();
		xqFactory.setProcessor(proc);
		XQItemType sType = xqFactory.createAtomicType(XQItemType.XQBASETYPE_STRING);
        response1.add(xqFactory.createItemFromAtomicValue("<response>constructed from security1500.xml</response>", sType));
        response1.add(xqFactory.createItemFromAtomicValue("<response>constructed from security5621.xml</response>", sType));
        response1.add(xqFactory.createItemFromAtomicValue("<response>constructed from security9012.xml</response>", sType));
		when(queryMgr.executeQuery(q1, null, null)).thenReturn(new FixedCursorImpl(response1));

    	QueryParams params = new QueryParams(q1, null, null);
    	Response response = target("query").request()
        		.header("Content-Type", "application/json")
        		.cookie(bg_cookie, "client-id")
        		.post(Entity.json(params));
    	ChunkedInput<String> input = response.readEntity(new GenericType<ChunkedInput<String>>() {});
    	input.setParser(ChunkedInput.createParser(QueryService.splitter));
    	String chunk;
    	Collection<String> results = new ArrayList<>();
    	while ((chunk = input.read()) != null) {
        	//System.out.println(chunk);
			results.add(chunk);
    	}    	
    	input.close();
    	//System.out.println(results);
    	assertEquals(3, results.size());
    	assertTrue(results.contains("<response>constructed from security1500.xml</response>"));
    	assertTrue(results.contains("<response>constructed from security5621.xml</response>"));
    	assertTrue(results.contains("<response>constructed from security9012.xml</response>"));
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
