package com.bagri.rest.service;

import static com.bagri.rest.RestConstants.bg_cookie;

import static org.junit.Assert.*;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.glassfish.jersey.test.JerseyTest;
import org.junit.Test;

import com.bagri.core.api.SchemaRepository;
import com.bagri.core.api.TransactionIsolation;
import com.bagri.core.api.TransactionManagement;
import com.bagri.core.api.BagriException;
import com.bagri.rest.BagriRestServer;
import com.bagri.rest.RepositoryProvider;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Properties;


public class TransactionServiceTest extends JerseyTest {
	
	private TransactionManagement txMgr;
	private SchemaRepository mockRepo;
	private RepositoryProvider mockPro;

    @Override
    protected Application configure() {
        txMgr = mock(TransactionManagement.class);
        mockRepo = mock(SchemaRepository.class);
    	mockPro = mock(RepositoryProvider.class);
        when(mockPro.getRepository("client-id")).thenReturn(mockRepo);
        when(mockRepo.getTxManagement()).thenReturn(txMgr);
        when(txMgr.isInTransaction()).thenReturn(false);
        try {
			when(txMgr.beginTransaction(TransactionIsolation.readCommited)).thenReturn(100L);
		} catch (BagriException ex) {
			ex.printStackTrace();
		}
        BagriRestServer server = new BagriRestServer(mockPro, null, new Properties());
        return server.buildConfig();
    }

    @Test
    public void testTransactionService() throws Exception {
        Boolean inTx = target("tx").request(MediaType.TEXT_PLAIN)
        		.cookie(bg_cookie, "client-id")
        		.get(Boolean.class);
        assertFalse(inTx);
        Entity<String> isolation = Entity.entity("readCommited", MediaType.TEXT_PLAIN);
        Response resp = target("tx").request(MediaType.TEXT_PLAIN)
        		.cookie(bg_cookie, "client-id")
        		.post(isolation);
        assertEquals(Status.CREATED.getStatusCode(), resp.getStatus());
        assertTrue(resp.getLocation().getPath().endsWith("/100"));
        long txId = resp.readEntity(Long.class);
        assertEquals(100L, txId);
        resp = target("tx").path("100").request(MediaType.TEXT_PLAIN)
				.cookie(bg_cookie, "client-id")
        		.delete();
        assertEquals(Status.OK.getStatusCode(), resp.getStatus());
        txId = target("tx").request(MediaType.TEXT_PLAIN)
        		.cookie(bg_cookie, "client-id")
        		.post(isolation, Long.class);
        assertEquals(100L, txId);
        Entity<Long> entity = Entity.entity(100L, MediaType.TEXT_PLAIN);
        resp = target("tx").path("100").request(MediaType.TEXT_PLAIN)
				.cookie(bg_cookie, "client-id")
        		.put(entity);
        assertEquals(Status.OK.getStatusCode(), resp.getStatus());
    }
	
}
