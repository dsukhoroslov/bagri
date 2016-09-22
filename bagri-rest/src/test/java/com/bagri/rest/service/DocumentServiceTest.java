package com.bagri.rest.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static com.bagri.xdm.common.Constants.xdm_document_data_format;
import static com.bagri.xdm.system.DataFormat.df_json;

import java.util.Properties;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;

import org.glassfish.jersey.test.JerseyTest;
import org.junit.Test;

import com.bagri.rest.BagriRestServer;
import com.bagri.rest.RepositoryProvider;
import com.bagri.rest.docs.DocumentResource;
import com.bagri.rest.docs.DocumentParams;
import com.bagri.xdm.api.DocumentManagement;
import com.bagri.xdm.api.SchemaRepository;
import com.bagri.xdm.api.XDMException;
import com.bagri.xdm.domain.Document;

public class DocumentServiceTest extends JerseyTest {
	
	private DocumentManagement docMgr;
	private SchemaRepository mockRepo;
	private RepositoryProvider mockPro;
	private Properties props;
	private Document response;

    @Override
    protected Application configure() {
    	props = new Properties();
    	props.setProperty(xdm_document_data_format, df_json);
        docMgr = mock(DocumentManagement.class);
        mockRepo = mock(SchemaRepository.class);
    	mockPro = mock(RepositoryProvider.class);
        when(mockPro.getRepository("default")).thenReturn(mockRepo);
        when(mockRepo.getDocumentManagement()).thenReturn(docMgr);
        response = new Document(1L, "a0001.xml", 0, "owner", 1, 18, 1);
        try {
			when(docMgr.storeDocumentFromString("a0001.xml", "<xml>content</xml>", props)).thenReturn(response);
		} catch (XDMException ex) {
			ex.printStackTrace();
		}
        BagriRestServer server = new BagriRestServer(mockPro, 3030);
        return server.buildConfig();
    }

    @Test
    public void testDocumentService() throws Exception {
    	
    	DocumentParams dp = new DocumentParams("a0001.xml", "<xml>content</xml>", props);
        DocumentResource doc = target("docs").request().header("Content-Type", "application/json").
        		post(Entity.json(dp), DocumentResource.class);
        assertEquals("a0001.xml", doc.uri);
        
        //target("tx").path("100").request(MediaType.TEXT_PLAIN).delete();
        //txId = target("tx").request(MediaType.TEXT_PLAIN).post(isolation, Long.class);
        //assertEquals(100L, txId);
        //Entity<Long> entity = Entity.entity(100L, MediaType.TEXT_PLAIN);
        //target("tx").path("100").request(MediaType.TEXT_PLAIN).put(entity);
    	
    }    

}
