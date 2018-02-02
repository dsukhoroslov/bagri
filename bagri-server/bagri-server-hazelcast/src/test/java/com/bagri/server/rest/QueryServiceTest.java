package com.bagri.server.rest;

import static com.bagri.rest.RestConstants.bg_cookie;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.glassfish.jersey.client.ChunkedInput;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.bagri.rest.service.QueryParams;
import com.bagri.rest.service.QueryService;

public class QueryServiceTest extends RestServiceTest {
	
	private String q1 = "for $doc in fn:collection(\"CLN_Security\")\n" +
			"return $doc";

	private String q2 = "declare namespace s=\"http://tpox-benchmark.com/security\";\n" +
			"declare variable $sym external;\n" +
			"for $doc in fn:collection(\"CLN_Security\")\n" +
			"where $sec/s:Symbol=$sym\n" +
			"return $doc";
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		//System.setProperty("bdb.log.level", "trace");
		startServices();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		stopServices();
	}

	@Test
    public void testQueryOperations() throws Exception {

		// add docs..
		
    	QueryParams params = new QueryParams(q1, null, null);

    	//int cnt = 0;
    	Collection<String> results = new ArrayList<>();
    	Response response = target.path("query").request()
        		.header("Content-Type", "application/json")
        		//.header("Keep-Alive", "timeout=5, max=5")
        		.cookie(bg_cookie, cuid)
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
    	
    	//assertEquals(3, results.size());
    	//assertTrue(results.contains("<response>constructed from security1500.xml</response>"));
    	//assertTrue(results.contains("<response>constructed from security5621.xml</response>"));
    	//assertTrue(results.contains("<response>constructed from security9012.xml</response>"));
	}    


}
