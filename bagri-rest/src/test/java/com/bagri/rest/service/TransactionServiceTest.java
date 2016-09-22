package com.bagri.rest.service;

import static org.junit.Assert.*;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;

import org.glassfish.jersey.test.JerseyTest;
import org.junit.Test;

import com.bagri.rest.BagriRestServer;
import com.bagri.rest.RepositoryProvider;
import com.bagri.xdm.api.SchemaRepository;
import com.bagri.xdm.api.TransactionIsolation;
import com.bagri.xdm.api.TransactionManagement;
import com.bagri.xdm.api.XDMException;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class TransactionServiceTest extends JerseyTest {
	
	private TransactionManagement txMgr;
	private SchemaRepository mockRepo;
	private RepositoryProvider mockPro;

    @Override
    protected Application configure() {
        txMgr = mock(TransactionManagement.class);
        mockRepo = mock(SchemaRepository.class);
    	mockPro = mock(RepositoryProvider.class);
        when(mockPro.getRepository("default")).thenReturn(mockRepo);
        when(mockRepo.getTxManagement()).thenReturn(txMgr);
        when(txMgr.isInTransaction()).thenReturn(false);
        try {
			when(txMgr.beginTransaction(TransactionIsolation.readCommited)).thenReturn(100L);
		} catch (XDMException ex) {
			ex.printStackTrace();
		}
        BagriRestServer server = new BagriRestServer(mockPro, 3030);
        return server.buildConfig();
    }

    @Test
    public void testTransactionService() throws Exception {
        Boolean inTx = target("tx").request(MediaType.TEXT_PLAIN).get(Boolean.class);
        assertFalse(inTx);
        Entity<String> isolation = Entity.entity("readCommited", MediaType.TEXT_PLAIN);
        long txId = target("tx").request(MediaType.TEXT_PLAIN).post(isolation, Long.class);
        assertEquals(100L, txId);
        target("tx").path("100").request(MediaType.TEXT_PLAIN).delete();
        txId = target("tx").request(MediaType.TEXT_PLAIN).post(isolation, Long.class);
        assertEquals(100L, txId);
        Entity<Long> entity = Entity.entity(100L, MediaType.TEXT_PLAIN);
        target("tx").path("100").request(MediaType.TEXT_PLAIN).put(entity);
    }
	
}
