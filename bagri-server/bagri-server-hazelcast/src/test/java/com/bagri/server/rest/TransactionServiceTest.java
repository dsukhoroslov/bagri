package com.bagri.server.rest;

import static com.bagri.rest.RestConstants.bg_cookie;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class TransactionServiceTest extends RestServiceTest {

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
    public void testTransactionOperations() throws Exception {
        Boolean inTx = target.path("tx").request(MediaType.TEXT_PLAIN)
        		.cookie(bg_cookie, cuid)
        		.get(Boolean.class);
        assertFalse(inTx);
        
        Entity<String> isolation = Entity.entity("readCommited", MediaType.TEXT_PLAIN);
        Response resp = target.path("tx").request(MediaType.TEXT_PLAIN)
        		.cookie(bg_cookie, cuid)
        		.post(isolation); // begin
        assertEquals(Status.CREATED.getStatusCode(), resp.getStatus());
        long txId = resp.readEntity(Long.class);
        assertTrue(txId > 0);

        inTx = target.path("tx").request(MediaType.TEXT_PLAIN)
        		.cookie(bg_cookie, cuid)
        		.get(Boolean.class);
        assertTrue(inTx);
        
        resp = target.path("tx").path(String.valueOf(txId)).request(MediaType.TEXT_PLAIN)
				.cookie(bg_cookie, cuid)
        		.delete(); // rollback
        assertEquals(Status.OK.getStatusCode(), resp.getStatus());

        inTx = target.path("tx").request(MediaType.TEXT_PLAIN)
        		.cookie(bg_cookie, cuid)
        		.get(Boolean.class);
        assertFalse(inTx);
        
        Long txId2 = target.path("tx").request(MediaType.TEXT_PLAIN)
        		.cookie(bg_cookie, cuid)
        		.post(isolation, Long.class); // begin
        assertTrue(txId2 > txId);

        inTx = target.path("tx").request(MediaType.TEXT_PLAIN)
        		.cookie(bg_cookie, cuid)
        		.get(Boolean.class);
        assertTrue(inTx);
        
        Entity<Long> entity = Entity.entity(txId2, MediaType.TEXT_PLAIN);
        resp = target.path("tx").path(String.valueOf(txId2)).request(MediaType.TEXT_PLAIN)
				.cookie(bg_cookie, cuid)
        		.put(entity); // commit
        assertEquals(Status.OK.getStatusCode(), resp.getStatus());
    }
	
}
