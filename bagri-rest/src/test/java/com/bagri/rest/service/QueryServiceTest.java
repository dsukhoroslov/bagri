package com.bagri.rest.service;

import com.bagri.client.hazelcast.impl.FixedCursorImpl;
import com.bagri.core.api.QueryManagement;
import com.bagri.core.api.SchemaRepository;
import com.bagri.core.xquery.api.XQProcessor;
import com.bagri.rest.BagriRestServer;
import com.bagri.rest.RepositoryProvider;
import com.bagri.xqj.BagriXQDataFactory;
import com.bagri.xquery.saxon.XQProcessorClient;
import org.glassfish.jersey.client.ChunkedInput;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.xml.xquery.XQItemAccessor;
import javax.xml.xquery.XQItemType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static com.bagri.rest.RestConstants.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

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
    	System.setProperty("sun.net.http.allowRestrictedHeaders", "true");
        queryMgr = mock(QueryManagement.class);
        mockRepo = mock(SchemaRepository.class);
    	mockPro = mock(RepositoryProvider.class);
        when(mockPro.getRepository("client-id")).thenReturn(mockRepo);
        when(mockRepo.getQueryManagement()).thenReturn(queryMgr);
        BagriRestServer server = new BagriRestServer(mockPro, null, new Properties());
        return server.buildConfig();
    }
    
    @Override
    public void tearDown() throws Exception {
    	super.tearDown();
    }
    
	@Test
    @SuppressWarnings("unchecked")
    public void testQueryService() throws Exception {
		
    	List<XQItemAccessor> response1 = new ArrayList<>();  
		XQProcessor proc = new XQProcessorClient();
		BagriXQDataFactory xqFactory = new BagriXQDataFactory();
		xqFactory.setProcessor(proc);
		XQItemType sType = xqFactory.createAtomicType(XQItemType.XQBASETYPE_STRING);
        response1.add(xqFactory.createItemFromAtomicValue("<response>constructed from security1500.xml</response>", sType));
        response1.add(xqFactory.createItemFromAtomicValue("<response>constructed from security5621.xml</response>", sType));
        response1.add(xqFactory.createItemFromAtomicValue("<response>constructed from security9012.xml</response>", sType));
		when(queryMgr.executeQuery(q1, null, null)).thenReturn(new FixedCursorImpl(response1));
    	QueryParams params = new QueryParams(q1, null, null);

    	//int cnt = 0;
    	Collection<String> results = new ArrayList<>();
    	// try it several times in order to solve jetty issue; #119
    	//do {
    		//results.clear();
	    	Response response = target("query").request()
	        		.header("Content-Type", "application/json")
	        		.header("Keep-Alive", "timeout=5, max=5")
	        		.cookie(bg_cookie, "client-id")
	        		.post(Entity.json(params));
	        assertEquals(Status.OK.getStatusCode(), response.getStatus());
	    	ChunkedInput<String> input = response.readEntity(new GenericType<ChunkedInput<String>>() {});
	    	input.setParser(ChunkedInput.createParser(QueryService.splitter));
	    	
	    	String chunk;
	    	while ((chunk = input.read()) != null) {
	        //	System.out.println(chunk);
				results.add(chunk);
	    	}    	
	    	input.close();
	    	//cnt++;
	    	//if (results.size() == 0) {
	    	//	System.out.println(response);
	    	//}
    	//} while (results.size() == 0 && cnt < 5);
    	
    	assertEquals(3, results.size());
    	assertTrue(results.contains("<response>constructed from security1500.xml</response>"));
    	assertTrue(results.contains("<response>constructed from security5621.xml</response>"));
    	assertTrue(results.contains("<response>constructed from security9012.xml</response>"));
	}    

	@Test
    @SuppressWarnings("unchecked")
    public void testQueryURIService() throws Exception {
    	//
    	List<String> response1 = Arrays.asList("security1500.xml", "security5621.xml", "security9012.xml");
		when(queryMgr.getDocumentUris(q1, null, null)).thenReturn(new FixedCursorImpl<>(response1));

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
    	List<String> response2 = Arrays.asList("security5621.xml");
		when(queryMgr.getDocumentUris(q2, params2, null)).thenReturn(new FixedCursorImpl<>(response2));
    	
    	params = new QueryParams(q2, params2, null);
        uris = target("query/uris").request()
        		.header("Content-Type", "application/json")
        		.cookie(bg_cookie, "client-id")
        		.post(Entity.json(params), Collection.class);
    	assertEquals(1, uris.size());
    	assertTrue(uris.contains("security5621.xml"));
    }
    
}
