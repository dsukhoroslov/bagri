package com.bagri.rest.service;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.ws.rs.core.Application;

import org.glassfish.jersey.test.JerseyTest;
import org.junit.Test;

import com.bagri.rest.BagriRestServer;
import com.bagri.rest.RepositoryProvider;
import com.bagri.xdm.api.QueryManagement;
import com.bagri.xdm.api.SchemaRepository;
import com.bagri.xdm.api.XDMException;

public class QueryServiceTest extends JerseyTest {
	
	private QueryManagement queryMgr;
	private SchemaRepository mockRepo;
	private RepositoryProvider mockPro;

    @Override
    protected Application configure() {
        queryMgr = mock(QueryManagement.class);
        mockRepo = mock(SchemaRepository.class);
    	mockPro = mock(RepositoryProvider.class);
        when(mockPro.getRepository("default")).thenReturn(mockRepo);
        when(mockRepo.getQueryManagement()).thenReturn(queryMgr);
        //when(txMgr.isInTransaction()).thenReturn(false);
        //try {
		//	when(txMgr.beginTransaction(TransactionIsolation.readCommited)).thenReturn(100L);
		//} catch (XDMException ex) {
		//	ex.printStackTrace();
		//}
        BagriRestServer server = new BagriRestServer(mockPro, 3030);
        return server.buildConfig();
    }

    @Test
    public void testQueryService() throws Exception {
    }
    
}
