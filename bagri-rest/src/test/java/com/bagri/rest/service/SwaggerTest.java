package com.bagri.rest.service;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Properties;

import javax.ws.rs.core.Application;

import org.glassfish.jersey.test.JerseyTest;
import org.junit.Test;

import com.bagri.core.api.SchemaRepository;
import com.bagri.rest.BagriRestServer;
import com.bagri.rest.RepositoryProvider;

public class SwaggerTest extends JerseyTest {

	private SchemaRepository mockRepo;
	private RepositoryProvider mockPro;
	private BagriRestServer server;

    //private JsonFactory jsonFactory = new JsonFactory(new ObjectMapper());

    @Override
    protected Application configure() {
        mockRepo = mock(SchemaRepository.class);
    	mockPro = mock(RepositoryProvider.class);
        when(mockPro.getRepository("client-id")).thenReturn(mockRepo);
    	when(mockRepo.getClientId()).thenReturn("client-id");
        //when(restContext.getSchemaManagement()).thenReturn(schemaManagement);
        server = new BagriRestServer(mockPro, null, new Properties());
        //server.start();
        return server.buildConfig();
    }

    @Test
    public void testScan() throws Exception {
    	server.start();
    	//server.reload("test", true);
    }

}
