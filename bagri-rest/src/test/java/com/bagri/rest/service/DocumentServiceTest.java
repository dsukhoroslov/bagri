package com.bagri.rest.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static com.bagri.xdm.common.Constants.xdm_document_data_format;
import static com.bagri.xdm.system.DataFormat.*;

import java.util.Properties;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;

import org.glassfish.jersey.test.JerseyTest;
import org.junit.Test;

import com.bagri.rest.BagriRestServer;
import com.bagri.rest.RepositoryProvider;
import com.bagri.xdm.api.DocumentManagement;
import com.bagri.xdm.api.SchemaRepository;
import com.bagri.xdm.api.XDMException;
import com.bagri.xdm.domain.Document;

public class DocumentServiceTest extends JerseyTest {
	
	private DocumentManagement docMgr;
	private SchemaRepository mockRepo;
	private RepositoryProvider mockPro;
	private Properties propsXml;
	private Properties propsJson;
	private Document responseXml;
	private Document responseJson;

    @Override
    protected Application configure() {
    	propsXml = new Properties();
    	propsXml.setProperty(xdm_document_data_format, df_xml);
    	propsJson = new Properties();
    	propsJson.setProperty(xdm_document_data_format, df_json);
        docMgr = mock(DocumentManagement.class);
        mockRepo = mock(SchemaRepository.class);
    	mockPro = mock(RepositoryProvider.class);
        when(mockPro.getRepository("default")).thenReturn(mockRepo);
        when(mockRepo.getDocumentManagement()).thenReturn(docMgr);
        responseXml = new Document(1L, "a0001.xml", 0, "owner", 1, 34, 1);
        responseJson = new Document(2L, "a0001.xml", 0, "owner", 1, 30, 1);
        try {
			when(docMgr.storeDocumentFromString("a0001.xml", "<content>initial content</content>", propsXml)).thenReturn(responseXml);
			when(docMgr.storeDocumentFromString("a0001.xml", "{\"content\": \"updated content\"}", propsJson)).thenReturn(responseJson);
			when(docMgr.getDocumentAsString("a0001.xml", propsXml)).thenReturn("<content>initial content</content>");
			when(docMgr.getDocumentAsString("a0001.xml", propsJson)).thenReturn("{\"content\": \"updated content\"}");
		} catch (XDMException ex) {
			ex.printStackTrace();
		}
        BagriRestServer server = new BagriRestServer(mockPro, 3030);
        return server.buildConfig();
    }

    @Test
    public void testDocumentService() throws Exception {
    	
    	// create document
    	DocumentParams params = new DocumentParams("a0001.xml", "<content>initial content</content>", propsXml);
        DocumentResource doc = target("docs").request().header("Content-Type", "application/json").
        		post(Entity.json(params), DocumentResource.class);
        assertEquals("a0001.xml", doc.uri);
        // get initial content
        String content = target("docs").path("a0001.xml").request(MediaType.APPLICATION_XML).get(String.class);
        assertEquals("<content>initial content</content>", content);
        
        // update document
        params = new DocumentParams("a0001.xml", "{\"content\": \"updated content\"}", propsJson);
        doc = target("docs").request().header("Content-Type", "application/json").
        		post(Entity.json(params), DocumentResource.class);
        assertEquals("a0001.xml", doc.uri);
        // get updated content
        content = target("docs").path("a0001.xml").request(MediaType.APPLICATION_JSON).get(String.class);
        assertEquals("{\"content\": \"updated content\"}", content);
        
        // delete document
        String uri = target("docs").path("a0001.xml").request().delete(String.class);
        assertEquals("a0001.xml", uri);
    }    

    @Test
    public void testDocumentPatterns() throws Exception {
    	// implement it..
    }
    
}
